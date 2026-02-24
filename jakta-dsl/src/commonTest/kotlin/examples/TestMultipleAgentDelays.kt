package examples

import TerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.jakta
import it.unibo.jakta.node.AgentBody
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlinx.coroutines.delay

class TestMultipleAgentDelays {
    val helloWorld =
        jakta {
            agent {
                body = object : AgentBody {}
                withSkills { TerminationSkillImpl(this@jakta.node) }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello...")
                        delay(10000)
                        agent.print("...World!")
                        skills.terminate()
                    }
                }
            }
            agent {
                body = object : AgentBody {}
                withSkills { TerminationSkillImpl(this@jakta.node) }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("I will be faster...")
                        delay(5000)
                        agent.print("...than you!")
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
