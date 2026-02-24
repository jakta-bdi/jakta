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

class TestHelloWorld {

    val helloWorld = jakta {
        agent("HelloAgent") {
            body = object : AgentBody {}
            withSkills { TerminationSkillImpl(this@jakta.node) }
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello World!")
                    skills.terminate()
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
