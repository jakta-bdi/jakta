package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import kotlin.test.Test

class TestHelloWorld {

    val helloWorld = node {
        agent {
            embodiedAs { Any() }
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("goal")
                } triggers {
                    agent.print("Hello World!")
                    node.terminateNode()
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
