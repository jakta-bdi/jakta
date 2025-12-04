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

class TestBeliefRemoval {
    val helloWorld =
        mas {
            environment { TestEnvironment() }
            agent {
                believes {
                    +"testBelief"
                }
                hasInitialGoals {
                    !"removeBelief"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("removeBelief")
                    } triggers {
                        agent.forget("testBelief")
                    }
                    removing.belief {
                        this.takeIf { it == "testBelief" }
                    } triggers {
                        agent.print("Belief removed: $context")
                        agent.terminate()
                    }
                }
            }
        }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun testBeliefRemoval() {
        executeInTestScope { helloWorld }
    }
}
