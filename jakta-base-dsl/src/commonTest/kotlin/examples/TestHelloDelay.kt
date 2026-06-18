package examples

import NodeTerminationSkill
import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import terminateNode

class TestHelloDelay {

    @Test
    fun testHelloDelay() {
        Logger.setMinSeverity(Severity.Debug)
        val timeToWait = 10.seconds

        executeInTestScope {
            node {
                context(NodeTerminationSkillImpl(node)) {
                    agent {
                        embodiedAs { Any() }
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
                                terminateNode()
                            }
                        }
                    }
                }
            }
        }
    }
}
