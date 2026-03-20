package examples

import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlinx.coroutines.delay

class TestHelloDelay {

    @Test
    fun testHelloDelay() {
        Logger.setMinSeverity(Severity.Debug)
        val timeToWait = 10000L

        executeInTestScope {
            node {
                agent {
                    body = object {}
                    withSkills { NodeTerminationSkillImpl(it) }
                    hasInitialGoals {
                        !"goal"
                    }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("goal")
                        } triggers {
                            agent.print("Hello...")
                            delay(timeToWait)
                            // TODO time will be a skill
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
