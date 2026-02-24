package examples

import TerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.jakta
import it.unibo.jakta.node.AgentBody
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefRemoval {
    val helloWorld =
        jakta {
            agent {
                body = object : AgentBody {}
                withSkills { TerminationSkillImpl(this@jakta.node) }
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
                        skills.terminate()
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
