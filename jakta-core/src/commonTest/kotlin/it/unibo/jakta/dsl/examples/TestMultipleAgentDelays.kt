package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.terminate
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

class TestMultipleAgentDelays {
    val helloWorld =
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello...")
                        delay(10.seconds)
                        agent.print("...World!")
                        node.terminateNode()
                    }
                }
            }
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("I will be faster...")
                        delay(5.seconds)
                        agent.print("...than you!")
                        context(node) {
                            agent.terminate()
                        }
                    }
                }
            }
        }

    @Test
    fun testMultipleAgentsDelays() {
        Logger.setMinSeverity(Severity.Error)
        executeInTestScope { helloWorld }
    }
}
