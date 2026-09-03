package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.goalQuery
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plans
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.askOneTo
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
import it.unibo.tuprolog.core.toAtom
import it.unibo.tuprolog.solve.Solution
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestKQMLMessaging {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    val bob = BaseAgentID("bob")
    val alice = BaseAgentID("alice")

    val startGoal = "start".toAtom()
    val delegatedGoal = "delegatedGoal".toAtom()

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
    fun `test tell`() = runTest {
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            agent.print("Hello!")
                            agent.tellTo(bob, belief { "ping"(1) })
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob) {
            plans { node ->
                prologPlan {
                    adding.belief {
                        matchingBelief { "ping"(1)[source(X)] }
                    } triggers {
                        agent.print("Hello, message received from ", X)
                        node.terminateNode()
                    }
                }
            }
        }

        run(aliceNode, bobNode)
    }

    @Test
    fun `test untell`() = runTest {
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            agent.print("Hello!")
                            agent.untellTo(bob, beliefQuery { "ping"(1) })
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob, initialBelief { "ping"(1)[source(alice)] }) {
            plans { node ->
                prologPlan {
                    removing.belief {
                        matchingBelief { "ping"(1)[source(X)] }
                    } triggers {
                        agent.print("Hello, message received from ", X)
                        node.terminateNode()
                    }
                }
            }
        }

        run(aliceNode, bobNode)
    }

    @Test
    fun `test achieve`() = runTest {
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            agent.print("Hello!")
                            agent.delegateAchieveTo(bob, goal { delegatedGoal })
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob) {
            plans { node ->
                prologPlan {
                    adding.goal {
                        matchingGoal { delegatedGoal[source(X)] }
                    } triggers {
                        agent.print("Hello, message received from ", X)
                        node.terminateNode()
                    }
                }
            }
        }

        run(aliceNode, bobNode)
    }

    // TODO is currently failing as the dropping of goals is not correctly implemented
    @Test
    fun `test unachieve`() = runTest {
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            agent.print("Hello! Waiting for bob to start and then stop him")
                            delay(3.seconds)
                            agent.sendUnachieveTo(bob, goalQuery { delegatedGoal })
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        val bobNode = masNode(bob) {
            plans { node ->
                prologPlan {
                    adding.goal {
                        matchingGoal { startGoal }
                    } triggers {
                        agent.print("Hello! I will start achieving the goal")
                        agent.achieve(goal { delegatedGoal })
                        node.terminateNode()
                    }
                }
                prologPlan {
                    adding.goal {
                        matchingGoal { delegatedGoal[source(X)] }
                    } triggers {
                        agent.print("Hello, achieving the goal from ", X)
                        delay(10.seconds)
                        node.terminateNode(RuntimeException("This goal should have been removed before completion"))
                    }
                }

                prologPlan {
                    removing.goal {
                        matchingGoal { delegatedGoal[source(X)] }
                    } triggers {
                        agent.print("Removing the goal. From ", X)
                    }
                }
            }
        }

        run(aliceNode, bobNode)
    }

    @Test
    fun `test askOne`() = runTest {
        val aliceNode = masNode(alice) {
            plans { node ->
                context(MessagingSkill(node)) {
                    prologPlan {
                        adding.goal {
                            matchingGoal { startGoal }
                        } triggers {
                            agent.print("Hello!")
                            delay(3.seconds)
                            val reply = agent.askOneTo(bob, beliefQuery { "b"(X) })
                            if (reply != null) {
                                agent.print("Received reply: ", X)
                            } else {
                                agent.print("No reply, I will stop.")
                            }
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
                            matchingGoal { replyOne(Q, M)[source(S)] }
                        } triggers {
                            agent.print("Reply to ", S)
                            agent.print("Id: ", M)
                            when (val solution = agent.beliefs.unifiesWith(Q.value())) {
                                is Solution.Yes -> {
                                    agent.print("Found a solution: ", solution.solvedQuery)
                                    this.context += solution.substitution
                                    val sender = BaseAgentID(id = S.value())
                                    val questionId = M.value<String>()
                                    agent.tellTo(sender, questionId, belief { solution.solvedQuery })
                                }

                                else -> agent.print("No matches.")
                            }
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        run(aliceNode, bobNode)
    }
}
