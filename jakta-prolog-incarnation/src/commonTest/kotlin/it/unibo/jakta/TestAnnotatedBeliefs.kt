package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.belief.BeliefBaseFactory
import it.unibo.jakta.dsl.belief.AnnotatedBeliefRevision
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.annotations
import it.unibo.jakta.dsl.belief.forgetAllMatching
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.belief.newContextBeliefQuery
import it.unibo.jakta.dsl.belief.replaceAll
import it.unibo.jakta.dsl.belief.replaceFrom
import it.unibo.jakta.dsl.belief.replaceSelf
import it.unibo.jakta.dsl.belief.sourcesOf
import it.unibo.jakta.dsl.belief.usesAnnotatedBeliefs
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plans
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
import it.unibo.jakta.event.UnlimitedChannelQueue
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.tellTo
import it.unibo.jakta.kqml.untellTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.toAtom
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

/**
 * Checks Jason's annotation semantics with [AnnotatedBeliefRevision]: a belief is identified by its term, and its
 * sources are a set that grows and shrinks.
 */
class TestAnnotatedBeliefs {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    private val ping = initialBelief { "ping"(1) }
    private val fromAlice = initialBelief { "ping"(1)[source("alice")] }
    private val fromCarol = initialBelief { "ping"(1)[source("carol")] }

    private fun annotatedBeliefBase(vararg beliefs: PrologBelief) =
        UnlimitedChannelQueue<AgentEvent.Internal.Belief<PrologBelief>>().let { events ->
            BeliefBaseFactory.of(events, beliefs.toList(), AnnotatedBeliefRevision) to events
        }

    private fun UnlimitedChannelQueue<AgentEvent.Internal.Belief<PrologBelief>>.drain() =
        generateSequence { tryNext() }.map { it to it.belief.annotations }.toList()

    private fun sources(vararg names: String): Set<Struct> = names.map { source(it.toAtom()) }.toSet()

    @Test
    fun `a belief told by two agents has both sources, and announces only the new one`() {
        val (beliefBase, events) = annotatedBeliefBase(fromAlice)
        beliefBase.add(fromCarol)
        assertEquals(sources("alice", "carol"), beliefBase.snapshot().single().annotations)
        val (event, announced) = events.drain().single()
        assertEquals(true, event is BeliefAddEvent)
        assertEquals(sources("carol"), announced)
    }

    @Test
    fun `telling a belief already told by the same agent changes nothing`() {
        val (beliefBase, events) = annotatedBeliefBase(fromAlice)
        assertEquals(false, beliefBase.add(fromAlice))
        assertEquals(emptyList(), events.drain())
    }

    @Test
    fun `an own belief and a told one are a single belief`() {
        val (beliefBase, _) = annotatedBeliefBase(ping, fromAlice)
        assertEquals(sources("self", "alice"), beliefBase.snapshot().single().annotations)
    }

    @Test
    fun `forgetting an own belief keeps what others told`() {
        val (beliefBase, events) = annotatedBeliefBase(ping, fromAlice)
        beliefBase.remove(ping)
        assertEquals(sources("alice"), beliefBase.snapshot().single().annotations)
        val (event, announced) = events.drain().single()
        assertEquals(true, event is BeliefRemoveEvent)
        assertEquals(sources("self"), announced)
    }

    @Test
    fun `removing the last source removes the belief`() {
        val (beliefBase, events) = annotatedBeliefBase(fromAlice, fromCarol)
        beliefBase.remove(fromAlice)
        beliefBase.remove(fromCarol)
        assertEquals(emptySet(), beliefBase.snapshot().toSet())
        assertEquals(listOf(sources("alice"), sources("carol")), events.drain().map { it.second })
    }

    @Test
    fun `removing a source the belief does not have changes nothing`() {
        val (beliefBase, events) = annotatedBeliefBase(fromAlice)
        assertEquals(false, beliefBase.remove(fromCarol))
        assertEquals(sources("alice"), beliefBase.snapshot().single().annotations)
        assertEquals(emptyList(), events.drain())
    }

    @Test
    fun `sourcesOf collects every source of the matching beliefs`() {
        val (beliefBase, _) = annotatedBeliefBase(ping, fromAlice, fromCarol, initialBelief { "pong"(1) })
        val query = newContextBeliefQuery { "ping"(X) }
        assertEquals(
            setOf("self", "alice", "carol"),
            beliefBase.snapshot().sourcesOf(query).map {
                it.toString()
            }.toSet(),
        )
    }

    @Test
    fun `without annotated beliefs, beliefs that differ only in their source collapse`() {
        val beliefBase = BeliefBaseFactory.of(UnlimitedChannelQueue(), listOf(fromAlice, fromCarol))
        assertEquals(sources("alice"), beliefBase.snapshot().single().annotations)
    }

    private val pingQuery = newContextBeliefQuery { "ping"(X) }

    private fun replaced(
        replace: it.unibo.jakta.event.AgentUpdate.Replace<PrologBelief>,
        vararg beliefs: PrologBelief,
    ): Pair<List<Pair<String, Set<Struct>>>, List<Pair<AgentEvent.Internal.Belief<PrologBelief>, Set<Struct>>>> {
        val (beliefBase, events) = annotatedBeliefBase(*beliefs)
        beliefBase.replace(replace.scope, replace.beliefs)
        val held = beliefBase.snapshot().map { it.head.toString() to it.annotations }.sortedBy { it.first }
        return held to events.drain()
    }

    @Test
    fun `a perception no longer seeing its own belief removes it`() {
        val (held, events) = replaced(replaceSelf(listOf(pingQuery), emptyList()), ping)
        assertEquals(emptyList(), held)
        assertEquals(listOf(sources("self")), events.map { it.second })
    }

    @Test
    fun `a perception no longer seeing a belief keeps what others told`() {
        val (held, events) = replaced(replaceSelf(listOf(pingQuery), emptyList()), ping, fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice")), held)
        assertEquals(true, events.single().first is BeliefRemoveEvent)
        assertEquals(sources("self"), events.single().second)
    }

    @Test
    fun `a perception does not retract what others told`() {
        val (held, events) = replaced(replaceSelf(listOf(pingQuery), emptyList()), fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice")), held)
        assertEquals(emptyList(), events)
    }

    @Test
    fun `a perception seeing its own belief again changes nothing`() {
        val (held, events) = replaced(replaceSelf(listOf(pingQuery), listOf(ping)), ping)
        assertEquals(listOf("ping(1)" to sources("self")), held)
        assertEquals(emptyList(), events)
    }

    @Test
    fun `a perception seeing what others told adds its own source`() {
        val (held, events) = replaced(replaceSelf(listOf(pingQuery), listOf(ping)), fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice", "self")), held)
        assertEquals(true, events.single().first is BeliefAddEvent)
        assertEquals(sources("self"), events.single().second)
    }

    @Test
    fun `a perception replaces the old value with the new one`() {
        val newPing = initialBelief { "ping"(2) }
        val (held, events) = replaced(
            replaceSelf(listOf(pingQuery), listOf(newPing)),
            ping,
            initialBelief {
                "pong"(1)
            },
        )
        assertEquals(listOf("ping(2)" to sources("self"), "pong(1)" to sources("self")), held)
        assertEquals(
            listOf("ping(1)" to false, "ping(2)" to true),
            events.map {
                it.first.belief.head.toString() to
                    (it.first is BeliefAddEvent)
            },
        )
    }

    @Test
    fun `a replacement from Alice replaces only what Alice told`() {
        val fromAlice2 = initialBelief { "ping"(2)[source("alice")] }
        val (held, _) = replaced(
            replaceFrom("alice".toAtom(), listOf(pingQuery), listOf(initialBelief { "ping"(3) })),
            fromAlice,
            fromAlice2,
            fromCarol,
            ping,
        )
        assertEquals(listOf("ping(1)" to sources("carol", "self"), "ping(3)" to sources("alice")), held)
    }

    @Test
    fun `replaceAll replaces every source`() {
        val (held, events) = replaced(replaceAll(listOf(pingQuery), emptyList()), ping, fromAlice, fromCarol)
        assertEquals(emptyList(), held)
        assertEquals(listOf(sources("self", "alice", "carol")), events.map { it.second })
    }

    @Test
    fun `without annotated beliefs, a perception replaces the agent's own beliefs only`() {
        val beliefBase = BeliefBaseFactory.of<PrologBelief>(
            UnlimitedChannelQueue(),
            listOf(
                ping,
                initialBelief {
                    "ping"(2)[source("alice")]
                },
            ),
        )
        val replace = replaceSelf(listOf(pingQuery), listOf(initialBelief { "ping"(3) }))
        beliefBase.replace(replace.scope, replace.beliefs)
        assertEquals(setOf("ping(2)", "ping(3)"), beliefBase.snapshot().map { it.head.toString() }.toSet())
    }

    private val alice: AgentID = BaseAgentID("alice")
    private val bob: AgentID = BaseAgentID("bob")
    private val carol: AgentID = BaseAgentID("carol")
    private val start = "start".toAtom()

    @Test
    fun `tells and untells from several agents keep track of every source`() = runTest {
        // what Bob's plans see: the source of each ping(1) added or removed, and the sources still believed afterwards
        val seen = mutableListOf<String>()

        fun teller(id: AgentID, wait: Int, untell: Boolean) = node(NodeBuilders.baseNode()) {
            agent(id) {
                embodiedAs { Any() }
                hasInitialGoals { !initialGoal { start } }
                withPredefinedPlans(
                    plans { node ->
                        context(MessagingSkill(node)) {
                            prologPlan {
                                adding.goal { matchingGoal { start } } triggers {
                                    delay(wait.seconds)
                                    agent.tellTo(bob, ping)
                                    if (untell) {
                                        delay(2.seconds)
                                        agent.untellTo(bob, newContextBeliefQuery { "ping"(1) })
                                    }
                                    node.terminateNode()
                                }
                            }
                        }
                    },
                )
            }
        }

        val bobNode = node(NodeBuilders.baseNode()) {
            agent(bob) {
                embodiedAs { Any() }
                usesAnnotatedBeliefs()
                handlesMessageEvents {
                    when (val payload = it.payload) {
                        is KQMLPayload -> handleKQMLPayload(payload, it.sender)
                        else -> null
                    }
                }
                withPredefinedPlans(
                    plans { node ->
                        prologPlan {
                            adding.belief { matchingBelief { "ping"(1)[source(S)] } } triggers {
                                seen += "+${BaseAgentID(id = S.value()).displayName()}"
                            }
                        }
                        prologPlan {
                            removing.belief { matchingBelief { "ping"(1)[source(S)] } } triggers {
                                val left = agent.beliefs.sourcesOf(newContextBeliefQuery { "ping"(1) })
                                seen += "-${BaseAgentID(id = S.value()).displayName()} leaving ${left.size}"
                                node.terminateNode()
                            }
                        }
                    },
                )
            }
        }

        run(teller(alice, wait = 1, untell = true), teller(carol, wait = 2, untell = false), bobNode)
        // Carol's tell is a new event, and Alice's untell leaves Carol's ping(1)
        assertEquals(listOf("+alice", "+carol", "-alice leaving 1"), seen)
    }

    @Test
    fun `forgetAllMatching forgets the matching beliefs whatever their sources`() = runTest {
        var left: List<String> = emptyList()
        val forgetful = node(NodeBuilders.baseNode()) {
            agent(alice) {
                embodiedAs { Any() }
                usesAnnotatedBeliefs()
                believes {
                    +ping
                    +fromCarol
                    +initialBelief { "ping"(2) }
                    +initialBelief { "pong"(1) }
                }
                hasInitialGoals { !initialGoal { start } }
                withPredefinedPlans(
                    plans { node ->
                        prologPlan {
                            adding.goal { matchingGoal { start } } triggers {
                                agent.forgetAllMatching(newContextBeliefQuery { "ping"(X) })
                                left = agent.beliefs.map { it.head.toString() }
                                node.terminateNode()
                            }
                        }
                    },
                )
            }
        }
        run(forgetful)
        assertEquals(listOf("pong(1)"), left)
    }

    private fun AgentID.displayName() = when (this) {
        alice -> "alice"
        carol -> "carol"
        else -> toString()
    }

    private suspend fun run(vararg nodes: ExecutableNode<Any>) = mas(NodeBuilders.baseNode()) {
        withNodes(*nodes)
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
