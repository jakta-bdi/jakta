package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefRemoval {
    val helloWorld =
        node {
            agent {
                embodiedAs { object {} }
                withSkills { BaseNodeTerminationSkill(it) }
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
                        skills.terminateNode()
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
