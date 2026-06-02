package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

class TestHelloDelay {

    @Test
    fun testHelloDelay() {
        Logger.setMinSeverity(Severity.Debug)
        val timeToWait = 10.seconds

        executeInTestScope<Any, NodeTerminationSkill> {
            node {
                agent {
                    embodiedAs { object {} }
                    withSkills { BaseNodeTerminationSkill(it) }
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
                            skills.terminateNode()
                        }
                    }
                }
            }
        }
    }
}
