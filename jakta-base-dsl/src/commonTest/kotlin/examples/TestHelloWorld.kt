package examples

import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test

class TestHelloWorld {

    val helloWorld = node {
        agent("HelloAgent") {
            body = object {}
            withSkills { NodeTerminationSkillImpl(it) }
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello World!")
                    skills.terminateNode()
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
