package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.NodeTerminationSkillImpl
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.delay

class TestConcurrentDelay {

    val helloWorld =
        node {
            agent {
                embodiedAs { object {} }
                withSkills { NodeTerminationSkillImpl(it) }
                hasInitialGoals {
                    !"goal"
                    !"anotherGoal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello...")
                        delay(10000)
                        agent.print("...World!")
                        skills.terminateNode()
                    }
                    adding.goal {
                        ifGoalMatch("anotherGoal")
                    } triggers {
                        delay(1000)
                        agent.print("Running while waiting...")
                        delay(5000)
                        agent.print("I'm still faster!")
                        skills.terminateNode()
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
