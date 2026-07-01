package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

class TestHelloDelay {

    @Test
    fun testHelloDelay() {
        Logger.setMinSeverity(Severity.Debug)
        val timeToWait = 10.seconds

        executeInTestScope<Any> {
            node {
                context(BaseNodeTerminationSkill(node)) {
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
                                delay(timeToWait)
                                // agent.print("Time perceived by the agent: ${environment.currentTime()}")
                                // assertEquals(environment.currentTime(), timeToWait)
                                agent.print("...World!")
                                terminateNode()
                            }
                        }
                    }
                }
            }
        }
    }
}
