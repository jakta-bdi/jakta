package examples

import TestEnvironment
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefPlan {
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent("TestBeliefAgent") {
                believes {
                    +"testBelief"
                }
                hasPlans {
                    adding.belief {
                        this.takeIf { it == "testBelief" }
                    } triggers {
                        agent.print("Belief added: $context")
                        agent.terminate()
                    }
                }
            }
        }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    @Test
    fun testBeliefAddition() {
        executeInTestScope { helloWorld }
    }
}
