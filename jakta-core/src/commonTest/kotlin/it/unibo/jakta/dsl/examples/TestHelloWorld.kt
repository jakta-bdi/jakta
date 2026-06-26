package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import kotlin.test.Test

class TestHelloWorld {

    val helloWorld = node {
        agent("HelloAgent") {
            embodiedAs { object {} }
            withSkills { BaseNodeTerminationSkill(it) }
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
