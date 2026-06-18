package examples

import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.node
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import terminateNode

class TestConcurrentDelay {

    val helloWorld =
        node {
            context(NodeTerminationSkillImpl(node)) {
                agent {
                    embodiedAs { Any() }
                    hasInitialGoals {
                        !"goal"
                        !"anotherGoal"
                    }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("goal")
                        } triggers {
                            agent.print("Hello...")
                            delay(1.seconds)
                            agent.print("...World!")
                            terminateNode()
                        }
                        adding.goal {
                            ifGoalMatch("anotherGoal")
                        } triggers {
                            delay(1.seconds)
                            agent.print("Running while waiting...")
                            delay(5.seconds)
                            agent.print("I'm still faster!")
                            terminateNode()
                        }
                    }
                }
            }
        }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Debug)
    }

    @Test
    fun testConcurrentDelay() {
        executeInTestScope { helloWorld }
    }
}
