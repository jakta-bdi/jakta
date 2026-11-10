package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.belief.BeliefBaseFactory
import it.unibo.jakta.dsl.belief.AnnotatedBeliefRevision
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.forgetAllMatching
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.belief.newContextBeliefQuery
import it.unibo.jakta.dsl.belief.replaceFrom
import it.unibo.jakta.dsl.belief.sourcesOf
import it.unibo.jakta.dsl.belief.update
import it.unibo.jakta.dsl.belief.usesAnnotatedBeliefs
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plans
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
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
    fun `adding annotations to a belief annotates its head, keeping those it has`() {
        val tagged = fromAlice.addAnnotations("certain".toAtom())
        assertEquals(setOf("certain".toAtom()) + sources("alice"), tagged.annotations)
        assertEquals(true, tagged.isFact)
    }

    private val certain = "certain".toAtom()

    @Test
    fun `a belief without a source is the agent's own`() {
        val (beliefBase, _) = annotatedBeliefBase(initialBelief { "ping"(1)[atomOf("certain")] })
        assertEquals(setOf(certain) + sources("self"), beliefBase.snapshot().single().annotations)
    }

    @Test
    fun `removing the last source removes the belief, whatever its other annotations`() {
        val certainFromAlice = initialBelief { "ping"(1)[atomOf("certain"), source("alice")] }
        val (beliefBase, events) = annotatedBeliefBase(certainFromAlice, fromCarol)
        beliefBase.remove(fromCarol)
        assertEquals(setOf(certain) + sources("alice"), beliefBase.snapshot().single().annotations)
        beliefBase.remove(fromAlice)
        assertEquals(emptySet(), beliefBase.snapshot().toSet())
        assertEquals(listOf(sources("carol"), setOf(certain) + sources("alice")), events.drain().map { it.second })
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
        replace: AgentUpdate.Replace<PrologBelief>,
        vararg beliefs: PrologBelief,
    ): Pair<List<Pair<String, Set<Struct>>>, List<Pair<AgentEvent.Internal.Belief<PrologBelief>, Set<Struct>>>> {
        val (beliefBase, events) = annotatedBeliefBase(*beliefs)
        beliefBase.replace(replace.replacements, replace.scope)
        val held = beliefBase.snapshot().map { it.head.toString() to it.annotations }.sortedBy { it.first }
        return held to events.drain()
    }

    @Test
    fun `a perception no longer seeing its own belief removes it`() {
        val (held, events) = replaced(replaceFrom(self, listOf(pingQuery), emptyList()), ping)
        assertEquals(emptyList(), held)
        assertEquals(listOf(sources("self")), events.map { it.second })
    }

    @Test
    fun `a perception no longer seeing a belief keeps what others told`() {
        val (held, events) = replaced(replaceFrom(self, listOf(pingQuery), emptyList()), ping, fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice")), held)
        assertEquals(true, events.single().first is BeliefRemoveEvent)
        assertEquals(sources("self"), events.single().second)
    }

    @Test
    fun `a perception does not retract what others told`() {
        val (held, events) = replaced(replaceFrom(self, listOf(pingQuery), emptyList()), fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice")), held)
        assertEquals(emptyList(), events)
    }

    @Test
    fun `a perception seeing its own belief again changes nothing`() {
        val (held, events) = replaced(replaceFrom(self, listOf(pingQuery), listOf(ping)), ping)
        assertEquals(listOf("ping(1)" to sources("self")), held)
        assertEquals(emptyList(), events)
    }

    @Test
    fun `a perception seeing what others told adds its own source`() {
        val (held, events) = replaced(replaceFrom(self, listOf(pingQuery), listOf(ping)), fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice", "self")), held)
        assertEquals(true, events.single().first is BeliefAddEvent)
        assertEquals(sources("self"), events.single().second)
    }

    @Test
    fun `a perception replaces the old value with the new one`() {
        val newPing = initialBelief { "ping"(2) }
        val (held, events) = replaced(
            replaceFrom(self, listOf(pingQuery), listOf(newPing)),
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
    fun `a perception marked with percept keeps what others told`() {
        val (held, _) = replaced(replaceFrom(percept, listOf(pingQuery), listOf(ping)), fromAlice)
        assertEquals(listOf("ping(1)" to sources("alice", "percept")), held)
    }

    @Test
    fun `a scope matching several annotations can remove only one source`() {
        // as Alice untelling ping(X)[certain]: the scope matches the whole belief, and gives only Alice's source
        val certainFromAlice = initialBelief { "ping"(1)[atomOf("certain"), source("alice")] }
        val query = newContextBeliefQuery { "ping"(X)[atomOf("certain"), source("alice")] }
        val untell = AgentUpdate.Replace<PrologBelief>(emptyList()) { belief ->
            belief.setAnnotations(source("alice")).takeIf { belief.matchBelief(query) != null }
        }
        val (held, events) = replaced(untell, certainFromAlice, fromCarol)
        assertEquals(listOf("ping(1)" to setOf("certain".toAtom()) + sources("carol")), held)
        assertEquals(listOf(sources("alice")), events.map { it.second })
    }

    @Test
    fun `a replacement of whole beliefs replaces every source`() {
        val all = AgentUpdate.Replace.where<PrologBelief>(emptyList()) { it.matchBelief(pingQuery) != null }
        val (held, events) = replaced(all, ping, fromAlice, fromCarol)
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
        val replace = replaceFrom(self, listOf(pingQuery), listOf(initialBelief { "ping"(3) }))
        beliefBase.replace(replace.replacements, replace.scope)
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

    private fun forgettingAgent(action: suspend MutableAgentState<PrologBelief, *>.() -> Unit) =
        node(NodeBuilders.baseNode()) {
            agent(alice) {
                embodiedAs { Any() }
                usesAnnotatedBeliefs()
                believes {
                    +ping
                    +fromCarol
                    +initialBelief { "ping"(2)[source("alice")] }
                    +initialBelief { "ping"(3)[source("alice"), source("carol")] }
                    +initialBelief { "ping"(4)[atomOf("certain")] }
                    +initialBelief { "ping"(5)[atomOf("certain"), source("alice")] }
                    +initialBelief { "pong"(1)[source("alice")] }
                }
                hasInitialGoals { !initialGoal { start } }
                withPredefinedPlans(
                    plans { node ->
                        prologPlan {
                            adding.goal { matchingGoal { start } } triggers {
                                agent.action()
                                node.terminateNode()
                            }
                        }
                    },
                )
            }
        }

    @Test
    fun `forgetAllMatching with a source forgets only that source, and the beliefs left without one`() = runTest {
        var left: List<PrologBelief> = emptyList()
        run(
            forgettingAgent {
                forgetAllMatching(newContextBeliefQuery { "ping"(X)[source("alice")] })
                left = beliefs.toList()
            },
        )
        assertEquals(
            listOf(
                "ping(1)" to sources("self", "carol"),
                "ping(3)" to sources("carol"),
                "ping(4)" to setOf(certain) + sources("self"),
                "pong(1)" to sources("alice"),
            ),
            left.map { it.head.toString() to it.annotations }.sortedBy { it.first },
        )
    }

    @Test
    fun `forgetAllMatching with a source variable forgets every matching belief`() = runTest {
        var left: List<PrologBelief> = emptyList()
        run(
            forgettingAgent {
                forgetAllMatching(newContextBeliefQuery { "ping"(X)[source(S)] })
                left = beliefs.toList()
            },
        )
        assertEquals(
            listOf("pong(1)" to sources("alice")),
            left.map { it.head.toString() to it.annotations },
        )
    }

    @Test
    fun `update replaces the agent's own belief with the same functor and arity, keeping what others told`() = runTest {
        var left: List<PrologBelief> = emptyList()
        val updating = node(NodeBuilders.baseNode()) {
            agent(alice) {
                embodiedAs { Any() }
                usesAnnotatedBeliefs()
                believes {
                    +ping
                    +initialBelief { "ping"(2)[source("carol")] }
                    +initialBelief { "ping"(1, 2) }
                }
                hasInitialGoals { !initialGoal { start } }
                withPredefinedPlans(
                    plans { node ->
                        prologPlan {
                            adding.goal { matchingGoal { start } } triggers {
                                agent.update(initialBelief { "ping"(3) })
                                left = agent.beliefs.toList()
                                node.terminateNode()
                            }
                        }
                    },
                )
            }
        }
        run(updating)
        assertEquals(
            listOf("ping(1, 2)" to sources("self"), "ping(2)" to sources("carol"), "ping(3)" to sources("self")),
            left.map { it.head.toString() to it.annotations }.sortedBy { it.first },
        )
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
