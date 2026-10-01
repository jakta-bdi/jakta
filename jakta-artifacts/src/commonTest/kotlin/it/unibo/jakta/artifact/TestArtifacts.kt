package it.unibo.jakta.artifact

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class Counter(name: String) : Artifact(name) {
    var count by observable(0)

    val inc by operation {
        count++
        if (count == 3) signal("three")
        count
    }

    // both changes are published together, at the end of the step
    val incTwice by operation {
        count++
        count++
    }

    // the change is rolled back when the operation fails
    val dec by operation {
        count--
        check(count >= 0) { "count is already 0" }
    }
}

class TupleSpace(name: String) : Artifact(name) {
    private val tuples = mutableListOf<String>()
    var size by observable(0)

    val write by operationWith { tuple: String ->
        tuples += tuple
        size = tuples.size
    }

    // blocks the caller's intention until a tuple with the prefix is available, then removes it
    val take by operationWith { prefix: String ->
        await { tuples.any { it.startsWith(prefix) } }
        tuples.first { it.startsWith(prefix) }.also {
            tuples -= it
            size = tuples.size
        }
    }
}

class Clock(name: String) : Artifact(name) {
    var ticks by observable(0)

    val start by operation {
        internalOperation {
            repeat(3) {
                delay(1.seconds)
                ticks++ // published when the step ends, at the next delay
            }
            signal("done")
        }
    }
}

// Observable properties become beliefs "name(value)", replacing the old value; signals become goals.
fun AgentState<String, String>.fromArtifacts(event: Perception): AgentUpdate<*>? = when (event) {
    is ArtifactEvent.PropertyChanged -> AgentUpdate.Belief(
        setOf("${event.property}(${event.value})"),
        beliefs.filter { it.startsWith("${event.property}(") }.toSet(),
    )

    is ArtifactEvent.PropertyRemoved -> AgentUpdate.Belief(
        emptySet(),
        beliefs.filter { it.startsWith("${event.property}(") }.toSet(),
    )

    is ArtifactEvent.Signal -> AgentUpdate.Goal(setOf(event.signal))

    else -> null
}

fun String.ifGoalMatch(goal: String): Unit? = Unit.takeIf { this == goal }

fun String.count(): Int? = Regex("""count\((\d+)\)""").matchEntire(this)?.groupValues?.get(1)?.toInt()

class TestArtifacts {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @Test
    fun agentsShareAnArtifactOnTheSameNode() = runTest {
        val observed = mutableListOf<Int>()
        val results = mutableListOf<Int>()
        var failure: String? = null
        var agentsOnTheNode = 0
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                node.makeArtifact(Counter("counter"))
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("observer")) {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { fromArtifacts(it) }
                        hasInitialGoals { !"observe" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("observe") } triggers {
                                agent.focus(artifacts.lookup("counter", ::Counter))
                            }
                            adding.belief { count() } triggers { observed += context }
                            adding.goal { ifGoalMatch("three") } triggers { node.terminateNode() }
                        }
                    }
                    agent<String, String>(BaseAgentID("user")) {
                        embodiedAs { Any() }
                        hasInitialGoals { !"use" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("use") } triggers {
                                agentsOnTheNode = node.agents.size
                                delay(1.seconds) // let the observer focus first
                                agent.achieve("decrement")
                                val counter = artifacts.lookup("counter", ::Counter)
                                repeat(3) { results += counter.inc() }
                            }
                            adding.goal { ifGoalMatch("decrement") } triggers {
                                artifacts.lookup("counter", ::Counter).dec()
                            }
                            failing.goal { ifGoalMatch("decrement") } triggers { failure = "dec failed" }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(0, 1, 2, 3), observed, "the failed dec is rolled back, so -1 is never perceived")
        assertEquals(listOf(1, 2, 3), results)
        assertEquals("dec failed", failure)
        assertEquals(2, agentsOnTheNode, "artifacts are not agents")
    }

    @Test
    fun changesArePerceivedAtTheEndOfEachStepAndForgottenWithStopFocus() = runTest {
        val observed = mutableListOf<Int>()
        val beliefsAfter = mutableMapOf<String, Collection<String>>()
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                val counter = node.makeArtifact(Counter("counter"))
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("agent")) {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { fromArtifacts(it) }
                        hasInitialGoals { !"use" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("use") } triggers {
                                agent.focus(counter)
                                beliefsAfter["focus"] = agent.beliefs.toList() // beliefs is a live view
                                counter.incTwice()
                                // the changes are perceived before the operation returns
                                beliefsAfter["incTwice"] = agent.beliefs.toList()
                                agent.stopFocus(counter)
                                beliefsAfter["stopFocus"] = agent.beliefs.toList()
                                counter.inc() // no longer perceived
                                node.terminateNode()
                            }
                            adding.belief { count() } triggers { observed += context }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(0, 2), observed)
        assertEquals(listOf("count(0)"), beliefsAfter["focus"]?.toList())
        assertEquals(listOf("count(2)"), beliefsAfter["incTwice"]?.toList())
        assertEquals(emptyList(), beliefsAfter["stopFocus"]?.toList())
    }

    @Test
    fun awaitSuspendsOnlyTheCallerIntention() = runTest {
        val log = mutableListOf<String>()
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                val space = node.makeArtifact(TupleSpace("space"))
                agent<String, String>(BaseAgentID("consumer")) {
                    embodiedAs { Any() }
                    hasInitialGoals {
                        !"consume"
                        !"stayReactive"
                    }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("consume") } triggers {
                            log += "took ${space.take("job")}"
                            node.terminateNode()
                        }
                        adding.goal { ifGoalMatch("stayReactive") } triggers { log += "reactive" }
                    }
                }
                agent<String, String>(BaseAgentID("producer")) {
                    embodiedAs { Any() }
                    hasInitialGoals { !"produce" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("produce") } triggers {
                            delay(1.seconds)
                            space.write("other")
                            space.write("job-1")
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf("reactive", "took job-1"), log)
    }

    @OptIn(ExperimentalCoroutinesApi::class) // currentTime
    @Test
    fun internalOperationsRunOnTheNodeTime() = runTest {
        val ticks = mutableListOf<Pair<String, Long>>()
        var doneAt: Long? = null
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                val clock = node.makeArtifact(Clock("clock"))
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("timer")) {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { event ->
                            (event as? ArtifactEvent.PropertyChanged)?.let { fromArtifacts(it) }
                        }
                        hasInitialGoals { !"time" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("time") } triggers {
                                agent.focus(clock)
                                clock.start()
                                agent.awaitSignal(clock, "done")
                                doneAt = testScheduler.currentTime
                                delay(1.seconds)
                                node.terminateNode()
                            }
                            adding.belief { takeIf { it.startsWith("ticks(") } } triggers {
                                ticks += context to testScheduler.currentTime
                            }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf("ticks(0)" to 0L, "ticks(1)" to 1000L, "ticks(2)" to 2000L, "ticks(3)" to 3000L), ticks)
        assertEquals(3000L, doneAt)
    }

    @Test
    fun agentsOnAnotherNodeUseTheArtifactThroughTheSkill() = runTest {
        val local = mutableListOf<Int>()
        val remote = mutableListOf<Int>()
        val results = mutableListOf<Int>()
        var failure: String? = null
        var beliefsAfterStopFocus: Collection<String>? = null
        lateinit var home: NodeID
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                home = node.id
                node.makeArtifact(Counter("counter"))
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("local")) {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { fromArtifacts(it) }
                        hasInitialGoals { !"observe" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("observe") } triggers {
                                agent.focus(artifacts.lookup("counter", ::Counter))
                            }
                            adding.belief { count() } triggers { local += context }
                        }
                    }
                }
            }
            node {
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("remote")) {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { fromArtifacts(it) }
                        hasInitialGoals { !"use" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("use") } triggers {
                                val counter = artifacts.lookup("counter", ::Counter) // hosted by the other node
                                agent.focus(counter)
                                try {
                                    counter.dec()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: ArtifactException) {
                                    failure = e.message // the failure of the operation on the other node
                                }
                                repeat(2) { results += counter.inc() }
                                agent.stopFocus(counter)
                                beliefsAfterStopFocus = agent.beliefs.toList()
                                delay(1.seconds) // let the agents of the other node perceive the last change
                                node.terminateNode(nodeID = home)
                                node.terminateNode()
                            }
                            adding.belief { count() } triggers { remote += context }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(0, 1, 2), local)
        assertEquals(listOf(0, 1, 2), remote)
        assertEquals(listOf(1, 2), results)
        assertEquals("count is already 0", failure)
        assertEquals(emptyList(), beliefsAfterStopFocus?.toList())
    }

    @Test
    fun remoteUsersFailWhenTheHomeNodeTerminates() = runTest {
        val failures = mutableListOf<String?>()
        var beliefsAfterFailure: Collection<String>? = null
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                node.makeArtifact(TupleSpace("space"))
                agent<String, String>(BaseAgentID("owner")) {
                    embodiedAs { Any() }
                    hasInitialGoals { !"leave" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("leave") } triggers {
                            delay(2.seconds)
                            node.terminateNode()
                        }
                    }
                }
            }
            node {
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("waiter")) {
                        embodiedAs { Any() }
                        handlesPerceptionEvents { fromArtifacts(it) }
                        hasInitialGoals { !"wait" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("wait") } triggers {
                                val space = artifacts.lookup("space", ::TupleSpace)
                                agent.focus(space)
                                repeat(2) {
                                    try {
                                        space.take("job") // never written: fails when the space goes away
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: ArtifactException) {
                                        failures += e.message
                                    }
                                }
                                beliefsAfterFailure = agent.beliefs.toList()
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf<String?>("space is no longer available", "space is no longer available"), failures)
        assertEquals(emptyList(), beliefsAfterFailure?.toList(), "the properties of the space are forgotten")
    }

    @Test
    fun lookingUpAMissingArtifactFails() = runTest {
        var failure: String? = null
        mas(NodeBuilders.artifactNode<Any>()) {
            node {
                context(ArtifactSkill(node)) {
                    agent<String, String>(BaseAgentID("seeker")) {
                        embodiedAs { Any() }
                        hasInitialGoals { !"seek" }
                        hasPlanLibrary {
                            adding.goal { ifGoalMatch("seek") } triggers {
                                try {
                                    artifacts.lookup("missing", ::Counter, timeout = 3.seconds)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: ArtifactException) {
                                    failure = e.message
                                }
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals("No node hosts an artifact named missing", failure)
    }
}
