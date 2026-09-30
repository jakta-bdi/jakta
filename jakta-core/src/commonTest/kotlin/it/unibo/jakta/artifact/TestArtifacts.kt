package it.unibo.jakta.artifact

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.ifGoalMatch
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

    val dec by operation {
        check(count > 0) { "count is already 0" }
        count--
    }
}

class TupleSpace(name: String) : Artifact(name) {
    private val tuples = mutableListOf<String>()

    val write by operationWith { tuple: String -> tuples += tuple }

    // blocks the caller's intention until a tuple with the prefix is available, then removes it
    val take by operationWith { prefix: String ->
        await { tuples.any { it.startsWith(prefix) } }
        tuples.first { it.startsWith(prefix) }.also { tuples -= it }
    }
}

class Clock(name: String) : Artifact(name) {
    val start by operation {
        internalOperation {
            for (tick in 1..3) {
                delay(1.seconds)
                signal("tick", tick)
            }
        }
    }
}

// Observable properties become beliefs "name(value)", replacing the old value; signals become goals.
fun AgentState<String, String>.fromArtifacts(event: Perception): AgentUpdate<*>? = when (event) {
    is ArtifactEvent.PropertyChanged -> AgentUpdate.Belief(
        setOf("${event.property}(${event.value})"),
        beliefs.filter { it.startsWith("${event.property}(") }.toSet(),
    )

    is ArtifactEvent.Signal -> AgentUpdate.Goal(setOf(event.signal))

    else -> null
}

fun String.count(): Int? = Regex("""count\((\d+)\)""").matchEntire(this)?.groupValues?.get(1)?.toInt()

class TestArtifacts {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @Test
    fun twoAgentsShareACounterOnTheSameNode() = runTest {
        val observed = mutableListOf<Int>()
        val results = mutableListOf<Int>()
        var failure: String? = null
        mas(NodeBuilders.baseNode<Any>()) {
            node {
                val counter = node.makeArtifact(Counter("counter"))
                agent<String, String>(BaseAgentID("observer")) {
                    embodiedAs { Any() }
                    handlesPerceptionEvents { fromArtifacts(it) }
                    hasInitialGoals { !"observe" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("observe") } triggers { agent.focus(counter) }
                        adding.belief { count() } triggers { observed += context }
                        adding.goal { ifGoalMatch("three") } triggers { node.terminateNode() }
                    }
                }
                agent<String, String>(BaseAgentID("user")) {
                    embodiedAs { Any() }
                    hasInitialGoals { !"use" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("use") } triggers {
                            delay(1.seconds) // let the observer focus first
                            agent.achieve("decrement")
                            repeat(3) { results += counter.inc() }
                        }
                        adding.goal { ifGoalMatch("decrement") } triggers { counter.dec() }
                        failing.goal { ifGoalMatch("decrement") } triggers { failure = "dec failed" }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(0, 1, 2, 3), observed)
        assertEquals(listOf(1, 2, 3), results)
        assertEquals("dec failed", failure)
    }

    @Test
    fun awaitSuspendsOnlyTheCallerIntention() = runTest {
        val log = mutableListOf<String>()
        mas(NodeBuilders.baseNode<Any>()) {
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
        val ticks = mutableListOf<Pair<Int, Long>>()
        mas(NodeBuilders.baseNode<Any>()) {
            node {
                val clock = node.makeArtifact(Clock("clock"))
                agent<String, String>(BaseAgentID("timer")) {
                    embodiedAs { Any() }
                    handlesPerceptionEvents { event ->
                        (event as? ArtifactEvent.Signal)?.let { AgentUpdate.Belief(setOf("tick(${it.value})")) }
                    }
                    hasInitialGoals { !"time" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("time") } triggers {
                            agent.focus(clock)
                            clock.start()
                        }
                        adding.belief { removePrefix("tick(").removeSuffix(")").toIntOrNull() } triggers {
                            ticks += context to testScheduler.currentTime
                            if (context == 3) node.terminateNode()
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(1 to 1000L, 2 to 2000L, 3 to 3000L), ticks)
    }

    @Test
    fun agentsOnAnotherNodeUseTheArtifactThroughAMirror() = runTest {
        val local = mutableListOf<Int>()
        val remote = mutableListOf<Int>()
        val results = mutableListOf<Int>()
        var failure: String? = null
        lateinit var home: NodeID
        mas(NodeBuilders.baseNode<Any>()) {
            node {
                home = node.id
                val counter = node.makeArtifact(Counter("counter"))
                agent<String, String>(BaseAgentID("local")) {
                    embodiedAs { Any() }
                    handlesPerceptionEvents { fromArtifacts(it) }
                    hasInitialGoals { !"observe" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("observe") } triggers { agent.focus(counter) }
                        adding.belief { count() } triggers { local += context }
                    }
                }
            }
            node {
                val counter = node.mirrorArtifact(Counter("counter"))
                agent<String, String>(BaseAgentID("remote")) {
                    embodiedAs { Any() }
                    handlesPerceptionEvents { fromArtifacts(it) }
                    hasInitialGoals { !"use" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("use") } triggers {
                            agent.focus(counter)
                            try {
                                counter.dec()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: IllegalStateException) {
                                failure = e.message // the failure of the operation on the other node
                            }
                            repeat(2) { results += counter.inc() }
                        }
                        adding.belief { count() } triggers {
                            remote += context
                            if (context == 2) {
                                node.terminateNode(nodeID = home)
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertEquals(listOf(0, 1, 2), local)
        assertEquals(listOf(0, 1, 2), remote)
        assertEquals(listOf(1, 2), results)
        assertEquals("count is already 0", failure)
    }
}
