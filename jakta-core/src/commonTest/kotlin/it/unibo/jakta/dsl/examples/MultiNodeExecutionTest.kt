package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNodeConnection
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class MultiNodeExecutionTest {

    val mas = mas(NodeBuilders.baseNode()) {
        node {
            agent(BaseAgentID("Alice")) {
                embodiedAs { Any() }
                hasInitialGoals { !"greet" }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("greet")
                    } triggers {
                        agent.print("Hello from Alice from node1: $node")
                        node.terminateNode()
                    }
                }
            }
        }

        node {
            agent(BaseAgentID("Bob")) {
                embodiedAs { Any() }
                hasInitialGoals { !"greet" }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("greet")
                    } triggers {
                        agent.print("Hello from Bob, from node2: $node")
                        node.terminateNode()
                    }
                }
            }
        }
    }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Info)
    }

    @Test
    fun testMultiNodeExecution() {
        runTest {
            mas.run(CoroutineNodeRunner(LocalNodeConnection()))
        }
    }
}
