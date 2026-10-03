package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.goal.replyAllTo
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plans
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.askAllTo
import it.unibo.jakta.kqml.askOneTo
import it.unibo.jakta.kqml.broadcastAskAll
import it.unibo.jakta.kqml.broadcastAskOne
import it.unibo.jakta.kqml.handleKQMLPayload
import it.unibo.jakta.kqml.replyTo
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.logic.allSolutionsOf
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.toAtom
import it.unibo.tuprolog.solve.Solution
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestKQMLAsk {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    val bob = BaseAgentID("bob")
    val alice = BaseAgentID("alice")
    val carol = BaseAgentID("carol")
    val dave = BaseAgentID("dave")

    val startGoal = "start".toAtom()

    fun masNode(
        id: AgentID,
        vararg beliefs: PrologBelief,
        block: () -> ((Node<Any>) -> List<Plan<PrologBelief, PrologGoal, *, *, *>>),
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
            hasInitialGoals {
                !initialGoal { startGoal }
            }
            withPredefinedPlans(block())
        }
    }

    suspend fun run(vararg nodes: ExecutableNode<Any>) = coroutineScope {
        val job = launch {
            mas(NodeBuilders.baseNode()) {
                withNodes(*nodes)
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }
        job.join()
    }

    @Test
    fun `test askOne`() = runTest {
        var answer: Int? = null
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            delay(1.seconds)
                            val reply = agent.askOneTo(bob, beliefQuery { "b"(X) }, timeout = 10.seconds)
                            if (reply != null && reply.isSuccess) answer = X.value<Int>()
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob, initialBelief { "b"(1) }) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { replyOne(Q, M)[source(S)] }
                        } triggers {
                            val solution = agent.beliefs.unifiesWith(Q.value<Struct>())
                            if (solution is Solution.Yes) {
                                val answer = belief { solution.solvedQuery }
                                agent.replyTo(BaseAgentID(id = S.value()), M.value<String>(), answer)
                            }
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        run(aliceNode, bobNode)
        assertEquals(1, answer)
    }

    @Test
    fun `test askOne timeout`() = runTest {
        var reply: Any? = "not asked yet"
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            delay(1.seconds)
                            reply = agent.askOneTo(bob, beliefQuery { "b"(X) }, timeout = 5.seconds)
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        // Bob never answers, and stops once Alice has given up
        val bobNode = masNode(bob) {
            plans { node ->
                prologPlan {
                    adding.goal {
                        matchingGoal { startGoal }
                    } triggers {
                        delay(10.seconds)
                        node.terminateNode()
                    }
                }
            }
        }

        run(aliceNode, bobNode)
        assertNull(reply)
    }

    @Test
    fun `test askAll receives every solution`() = runTest {
        var replies: List<Substitution>? = null
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            replies = agent.askAllTo(bob, beliefQuery { "b"(X) }, timeout = 10.seconds)
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob, initialBelief { "b"(1) }, initialBelief { "b"(2) }) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { replyAllTo(Q, M)[source(S)] }
                        } triggers {
                            val answers = agent.beliefs.allSolutionsOf(Q.value<Struct>())
                                .filterIsInstance<Solution.Yes>()
                                .map { belief { it.solvedQuery } }
                            agent.replyTo(BaseAgentID(id = S.value()), M.value<String>(), *answers.toTypedArray())
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        run(aliceNode, bobNode)
        assertEquals(setOf("1", "2"), replies?.map { it.getByName("X").toString() }?.toSet())
    }

    @Test
    fun `test askOne to many receivers`() = runTest {
        var replies: Map<AgentID, Substitution>? = null
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            delay(1.seconds)
                            replies = agent.askOneTo(listOf(bob, carol), beliefQuery { "b"(X) }, timeout = 10.seconds)
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val receiver = { id: BaseAgentID, n: Int ->
            masNode(id, initialBelief { "b"(n) }) {
                plans { node ->
                    context(MessagingSkill(node)) {
                        prologPlan {
                            adding.goal {
                                matchingGoal { replyOne(Q, M)[source(S)] }
                            } triggers {
                                val solution = agent.beliefs.unifiesWith(Q.value<Struct>()) as Solution.Yes
                                val answer = belief { solution.solvedQuery }
                                agent.replyTo(BaseAgentID(id = S.value()), M.value<String>(), answer)
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }

        run(aliceNode, receiver(bob, 1), receiver(carol, 2))
        assertEquals(
            mapOf<AgentID, String>(bob to "1", carol to "2"),
            replies?.mapValues { it.value.getByName("X").toString() },
        )
    }

    @Test
    fun `test askAll to many receivers returns the replies received before the timeout`() = runTest {
        var replies: Map<AgentID, List<Substitution>>? = null
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            delay(1.seconds)
                            replies = agent.askAllTo(listOf(bob, carol), beliefQuery { "b"(X) }, timeout = 5.seconds)
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob, initialBelief { "b"(1) }, initialBelief { "b"(2) }) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { replyAllTo(Q, M)[source(S)] }
                        } triggers {
                            val answers = agent.beliefs.allSolutionsOf(Q.value<Struct>())
                                .filterIsInstance<Solution.Yes>()
                                .map { belief { it.solvedQuery } }
                            agent.replyTo(BaseAgentID(id = S.value()), M.value<String>(), *answers.toTypedArray())
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        // Carol never answers, and stops once Alice has given up
        val carolNode = masNode(carol) {
            plans { node ->
                prologPlan {
                    adding.goal {
                        matchingGoal { startGoal }
                    } triggers {
                        delay(10.seconds)
                        node.terminateNode()
                    }
                }
            }
        }

        run(aliceNode, bobNode, carolNode)
        assertEquals(
            mapOf<AgentID, Set<String>>(bob to setOf("1", "2")),
            replies?.mapValues { (_, answers) -> answers.map { it.getByName("X").toString() }.toSet() },
        )
    }

    @Test
    fun `test broadcastAskOne binds the first reply, and drops the others`() = runTest {
        var replies: Map<AgentID, Substitution>? = null
        var bound: Int? = null
        var beliefsFromReplies: Int? = null
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            delay(1.seconds)
                            replies = agent.broadcastAskOne(beliefQuery { "b"(X) }, timeout = 10.seconds)
                            bound = X.value<Int>()
                            // Carol's late reply arrives meanwhile
                            delay(3.seconds)
                            beliefsFromReplies = agent.beliefs.count { it.head.functor == "b" }
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        // Bob answers right away, Carol later
        val receiver = { id: BaseAgentID, n: Int, after: Int ->
            masNode(id, initialBelief { "b"(n) }) {
                plans { node ->
                    context(MessagingSkill(node)) {
                        prologPlan {
                            adding.goal {
                                matchingGoal { replyOne(Q, M)[source(S)] }
                            } triggers {
                                delay(after.seconds)
                                val solution = agent.beliefs.unifiesWith(Q.value<Struct>()) as Solution.Yes
                                val answer = belief { solution.solvedQuery }
                                agent.replyTo(BaseAgentID(id = S.value()), M.value<String>(), answer)
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }

        run(aliceNode, receiver(bob, 1, 0), receiver(carol, 2, 2))
        assertEquals(mapOf<AgentID, String>(bob to "1"), replies?.mapValues { it.value.getByName("X").toString() })
        assertEquals(1, bound)
        assertEquals(0, beliefsFromReplies)
    }

    @Test
    fun `test broadcastAskAll waits for the given number of replies`() = runTest {
        var replies: Map<AgentID, List<Substitution>>? = null
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            delay(1.seconds)
                            replies = agent.broadcastAskAll(beliefQuery { "b"(X) }, timeout = 10.seconds, replies = 2)
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        // Bob and Carol answer right away, Dave later
        val receiver = { id: BaseAgentID, n: Int, after: Int ->
            masNode(id, initialBelief { "b"(n) }) {
                plans { node ->
                    context(MessagingSkill(node)) {
                        prologPlan {
                            adding.goal {
                                matchingGoal { replyAllTo(Q, M)[source(S)] }
                            } triggers {
                                delay(after.seconds)
                                val answers = agent.beliefs.allSolutionsOf(Q.value<Struct>())
                                    .filterIsInstance<Solution.Yes>()
                                    .map { belief { it.solvedQuery } }
                                agent.replyTo(BaseAgentID(id = S.value()), M.value<String>(), *answers.toTypedArray())
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }

        run(aliceNode, receiver(bob, 1, 0), receiver(carol, 2, 0), receiver(dave, 3, 2))
        assertEquals(
            mapOf<AgentID, List<String>>(bob to listOf("1"), carol to listOf("2")),
            replies?.mapValues { (_, answers) -> answers.map { it.getByName("X").toString() } },
        )
    }
}
