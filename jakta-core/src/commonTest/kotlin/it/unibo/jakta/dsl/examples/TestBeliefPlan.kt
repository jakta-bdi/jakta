package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.NodeTerminationSkillImpl
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefPlan {

    val helloWorld = node {
        agent("Hello world agent") {
            embodiedAs { object {} }
            withSkills { NodeTerminationSkillImpl(it) }
            hasInitialGoals {
                ! "start"
            }
            hasPlans {
                adding.goal {
                    this.takeIf{ it == "start" }
                } triggers {
                    agent.believe("testBelief")
                }

                adding.belief {
                    this.takeIf { it == "testBelief" }
                } triggers {
                    agent.print("Belief added: $context")
                    skills.terminateNode()
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
