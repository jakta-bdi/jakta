package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.delay

class TestHelloDelay {

    @Test
    fun testHelloDelay() {
        Logger.setMinSeverity(Severity.Debug)
        val timeToWait = 10000L

        executeInTestScope {
            mas {
                environment { TestEnvironment() }
                agent {
                    hasInitialGoals {
                        !"goal"
                    }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("goal")
                        } triggers {
                            agent.print("Hello...")
                            delay(timeToWait)
                            agent.print("Time perceived by the agent: ${environment.currentTime()}")
                            assertEquals(environment.currentTime(), timeToWait)
                            agent.print("...World!")
                            agent.terminate()
                        }
                    }
                }
            }
        }
    }
}
