package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.belief.newContextBeliefQuery
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plans
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.askOneTo
import it.unibo.jakta.kqml.broadcastAchieve
import it.unibo.jakta.kqml.broadcastTell
import it.unibo.jakta.kqml.broadcastUnachieve
import it.unibo.jakta.kqml.broadcastUntell
import it.unibo.jakta.kqml.delegateAchieveTo
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.sendUnachieveTo
import it.unibo.jakta.kqml.tellTo
import it.unibo.jakta.kqml.untellTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.toAtom
import it.unibo.tuprolog.solve.Solution
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

/**
 * Checks what each KQML performative does on its receivers: Alice sends, Bob (and Carol, for broadcasts) receive.
 */
class TestKQMLPerformatives {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    private val alice: AgentID = BaseAgentID("alice")
    private val bob: AgentID = BaseAgentID("bob")
    private val carol: AgentID = BaseAgentID("carol")
    private val start = "start".toAtom()

    /**
     * A node with a single agent that handles KQML messages, starts with the goal `start`, and has [plans].
     */
    private fun kqmlNode(
        id: AgentID,
        vararg beliefs: PrologBelief,
        plans: () -> ((Node<Any>) -> List<Plan<PrologBelief, PrologGoal, *, *, *>>),
    ) = node(NodeBuilders.baseNode()) {
        agent(id) {
            embodiedAs { Any() }
            handlesMessageEvents {
                when (val payload = it.payload) {
                    is KQMLPayload -> handleKQMLPayload(payload, it.sender)
                    else -> null
                }
            }
            beliefs.forEach { addBelief(it) }
            hasInitialGoals { !initialGoal { start } }
            withPredefinedPlans(plans())
        }
    }

    private suspend fun run(vararg nodes: ExecutableNode<Any>) = mas(NodeBuilders.baseNode()) {
        withNodes(*nodes)
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))

    /**
     * A receiver recording, in [received], the source of each `ping(N)` belief it is told, and stopping.
     */
    private fun pingReceiver(id: AgentID, received: MutableList<Pair<AgentID, AgentID>>) = kqmlNode(id) {
        plans { node ->
            prologPlan {
                adding.belief { matchingBelief { "ping"(N)[source(S)] } } triggers {
                    received += id to BaseAgentID(id = S.value())
                    node.terminateNode()
                }
            }
        }
    }

    /**
     * A receiver with [beliefs], recording in [removed] the source of the first `ping` belief it forgets and how many
     * `ping` beliefs it still has, and stopping.
     */
    private fun untellReceiver(id: AgentID, removed: MutableList<Pair<AgentID, Int>>, vararg beliefs: PrologBelief) =
        kqmlNode(id, *beliefs) {
            plans { node ->
                prologPlan {
                    removing.belief { matchingBelief { "ping"(N)[source(S)] } } triggers {
                        removed += BaseAgentID(id = S.value()) to agent.beliefs.count { it.head.functor == "ping" }
                        node.terminateNode()
                    }
                }
            }
        }

    /**
     * A receiver recording in [adopted] the source of each `job(N)` goal it is delegated, and stopping.
     */
    private fun jobReceiver(id: AgentID, adopted: MutableList<Pair<AgentID, AgentID>>) = kqmlNode(id) {
        plans { node ->
            prologPlan {
                adding.goal { matchingGoal { "job"(N)[source(S)] } } triggers {
                    adopted += id to BaseAgentID(id = S.value())
                    node.terminateNode()
                }
            }
        }
    }

    /**
     * A receiver pursuing each delegated `job(N)` goal for a while, recording in [dropped] the source of the goals
     * removed meanwhile, and stopping.
     */
    private fun unachieveReceiver(id: AgentID, dropped: MutableList<Pair<AgentID, AgentID>>) = kqmlNode(id) {
        plans { node ->
            prologPlan {
                adding.goal { matchingGoal { "job"(N)[source(S)] } } triggers {
                    delay(10.seconds)
                }
            }
            prologPlan {
                removing.goal { matchingGoal { "job"(N)[source(S)] } } triggers {
                    dropped += id to BaseAgentID(id = S.value())
                    node.terminateNode()
                }
            }
        }
    }

    /**
     * Alice: once every agent is running, does each of [steps] with a [MessagingSkill], a second apart, then stops.
     */
    private fun sender(vararg steps: context(MessagingSkill) (Agent) -> Unit) = kqmlNode(alice) {
        plans { node ->
            context(MessagingSkill(node)) {
                prologPlan {
                    adding.goal { matchingGoal { start } } triggers {
                        for (step in steps) {
                            delay(1.seconds)
                            step(agent)
                        }
                        node.terminateNode()
                    }
                }
            }
        }
    }

    private val ping = initialBelief { "ping"(1) }
    private val pingQuery = newContextBeliefQuery { "ping"(1) }
    private val job = initialGoal { "job"(1) }

    @Test
    fun `tellTo adds the belief to the receiver, with the sender as source`() = runTest {
        val received = mutableListOf<Pair<AgentID, AgentID>>()
        run(sender({ it.tellTo(bob, ping) }), pingReceiver(bob, received))
        assertEquals(listOf(bob to alice), received)
    }

    @Test
    fun `broadcastTell adds the belief to every other agent`() = runTest {
        val received = mutableListOf<Pair<AgentID, AgentID>>()
        run(sender({ it.broadcastTell(ping) }), pingReceiver(bob, received), pingReceiver(carol, received))
        assertEquals(setOf(bob to alice, carol to alice), received.toSet())
    }

    @Test
    fun `untellTo removes only the beliefs told by the sender`() = runTest {
        val removed = mutableListOf<Pair<AgentID, Int>>()
        run(
            sender({ it.untellTo(bob, newContextBeliefQuery { "ping"(N) }) }),
            untellReceiver(
                bob,
                removed,
                initialBelief {
                    "ping"(1)[source(carol)]
                },
                initialBelief { "ping"(2)[source(alice)] },
            ),
        )
        // Alice's ping(2) is gone, Carol's ping(1) is still there
        assertEquals(listOf(alice to 1), removed)
    }

    @Test
    fun `broadcastUntell removes the beliefs told by the sender from every other agent`() = runTest {
        val removed = mutableListOf<Pair<AgentID, Int>>()
        val alicesPing = initialBelief { "ping"(1)[source(alice)] }
        run(
            sender({ it.broadcastUntell(pingQuery) }),
            untellReceiver(bob, removed, alicesPing),
            untellReceiver(carol, removed, alicesPing),
        )
        assertEquals(listOf(alice to 0, alice to 0), removed)
    }

    @Test
    fun `delegateAchieveTo adds the goal to the receiver, with the sender as source`() = runTest {
        val adopted = mutableListOf<Pair<AgentID, AgentID>>()
        run(sender({ it.delegateAchieveTo(bob, job) }), jobReceiver(bob, adopted))
        assertEquals(listOf(bob to alice), adopted)
    }

    @Test
    fun `broadcastAchieve adds the goal to every other agent`() = runTest {
        val adopted = mutableListOf<Pair<AgentID, AgentID>>()
        run(sender({ it.broadcastAchieve(job) }), jobReceiver(bob, adopted), jobReceiver(carol, adopted))
        assertEquals(setOf(bob to alice, carol to alice), adopted.toSet())
    }

    @Test
    fun `sendUnachieveTo removes the goal delegated by the sender`() = runTest {
        val dropped = mutableListOf<Pair<AgentID, AgentID>>()
        run(
            sender({ it.delegateAchieveTo(bob, job) }, { it.sendUnachieveTo(bob, job) }),
            unachieveReceiver(bob, dropped),
        )
        assertEquals(listOf(bob to alice), dropped)
    }

    @Test
    fun `broadcastUnachieve removes the goal delegated by the sender from every other agent`() = runTest {
        val dropped = mutableListOf<Pair<AgentID, AgentID>>()
        run(
            sender({ it.broadcastAchieve(job) }, { it.broadcastUnachieve(job) }),
            unachieveReceiver(bob, dropped),
            unachieveReceiver(carol, dropped),
        )
        assertEquals(setOf(bob to alice, carol to alice), dropped.toSet())
    }

    @Test
    fun `askOneTo binds the query to the answer of the receiver`() = runTest {
        var answer: String? = null
        val asker = kqmlNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal { matchingGoal { start } } triggers {
                            delay(1.seconds)
                            val reply = agent.askOneTo(bob, beliefQuery { "b"(X) }, timeout = 10.seconds)
                            if (reply != null && reply.isSuccess) answer = X.value<Int>().toString()
                            node.terminateNode()
                        }
                    }
                }
            }
        }
        val answerer = kqmlNode(bob, initialBelief { "b"(1) }) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal { matchingGoal { replyOne(Q, M)[source(S)] } } triggers {
                            val solution = agent.beliefs.unifiesWith(Q.value<Struct>())
                            if (solution is Solution.Yes) {
                                agent.tellTo(
                                    BaseAgentID(id = S.value()),
                                    M.value<String>(),
                                    belief {
                                        solution.solvedQuery
                                    },
                                )
                            }
                            node.terminateNode()
                        }
                    }
                }
            }
        }
        run(asker, answerer)
        assertEquals("1", answer)
    }

    @Test
    fun `askOneTo returns null when no answer arrives in time`() = runTest {
        var reply: Any? = "not asked yet"
        val asker = kqmlNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal { matchingGoal { start } } triggers {
                            delay(1.seconds)
                            reply = agent.askOneTo(bob, beliefQuery { "b"(X) }, timeout = 5.seconds)
                            node.terminateNode()
                        }
                    }
                }
            }
        }
        // Bob never answers, and stops once Alice has given up
        val silent = kqmlNode(bob) {
            plans { node ->
                prologPlan {
                    adding.goal { matchingGoal { start } } triggers {
                        delay(10.seconds)
                        node.terminateNode()
                    }
                }
            }
        }
        run(asker, silent)
        assertNull(reply)
    }
}
