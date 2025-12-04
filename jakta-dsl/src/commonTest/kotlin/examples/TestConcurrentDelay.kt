package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.delay

class TestConcurrentDelay {

    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent {
                hasInitialGoals {
                    !"goal"
                    !"anotherGoal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello...")
                        delay(10000)
                        agent.print("...World!")
                        agent.terminate()
                    }
                    adding.goal {
                        ifGoalMatch("anotherGoal")
                    } triggers {
                        delay(1000)
                        agent.print("Running while waiting...")
                        delay(5000)
                        agent.print("I'm still faster!")
                    }
                }
            }
        }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @Test
    fun testConcurrentDelay() {
        executeInTestScope { helloWorld }
    }
}
