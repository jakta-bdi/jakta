package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.Test

class TestHelloWorld {

    val helloWorld = mas {
        environment { TestEnvironment() }
        agent("HelloAgent") {
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello World!")
                    agent.terminate()
                }
            }
        }
    }

    @Test
    fun testHelloWorld() {
        Logger.setMinSeverity(Severity.Debug)
        executeInTestScope { helloWorld }
    }
}
