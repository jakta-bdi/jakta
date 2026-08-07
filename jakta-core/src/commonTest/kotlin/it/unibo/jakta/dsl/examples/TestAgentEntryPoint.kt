package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plans
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNodeConnection
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TestAgentEntryPoint {

    val aliceID = BaseAgentID("Alice")
    val bobID = BaseAgentID("Bob")

    val alice = agent<String, String, Any>(aliceID) {
        embodiedAs { Any() }
        handlesMessageEvents {
            when (it.payload) {
                is String -> AgentUpdate.Goal(setOf("greet"))
                else -> null
            }
        }
        hasPlanLibrary {
            adding.goal {
                ifGoalMatch("greet")
            } triggers {
                agent.print("Hello from Alice")
                node.terminateNode()
            }
        }
    }

    context(messaging: MessagingSkill)
    val bob: (Node<Any>) -> AgentSpecification<Any, String, Any>
        get() = agent(bobID) {
            embodiedAs { Any() }
            hasInitialGoals { !"greet" }
            hasPlanLibrary {
                adding.goal {
                    ifGoalMatch("greet")
                } triggers {
                    agent.sendTo(aliceID, "greet")
                    agent.print("Hello from Bob")
                    node.terminateNode()
                }
            }
        }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Info)
    }

    @Test
    fun testWithAgentConfigurationSyntax() {
        runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(alice)
                }
                node {
                    context(MessagingSkill(node)) {
                        withAgents(bob)
                    }
                }
            }.run(CoroutineNodeRunner(LocalNodeConnection()))
        }
    }

    @Test
    fun testPlansEntryPoint() {

        val myCustomPlans = plans<String, String, Any> { node ->
            adding.goal {
                ifGoalMatch("hello")
            } triggers {
                agent.print("Hello!")
                node.terminateNode()
            }
        }

        val myOtherPlans = plans<String, String, Any> { node ->
            adding.goal {
                ifGoalMatch("ciao")
            } triggers {
                agent.print("Ciao!")
                node.terminateNode()
            }
        }

        val myAgent = agent {
            embodiedAs { Any() }
            hasInitialGoals {
                !"ciao"
                !"hello"
            }
            withPredefinedPlans(myCustomPlans, myOtherPlans)
        }

        runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(myAgent)
                }
            }.run(CoroutineNodeRunner(LocalNodeConnection()))
        }
    }
}
