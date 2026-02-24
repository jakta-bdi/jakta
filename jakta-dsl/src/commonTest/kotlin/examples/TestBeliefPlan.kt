package examples

import TerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import it.unibo.jakta.jakta
import it.unibo.jakta.node.AgentBody
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefPlan {

    val helloWorld = jakta {
        agent("Hello world agent") {
            body = object : AgentBody {}
            withSkills { TerminationSkillImpl(this@jakta.node) }
            believes {
                +"testBelief"
            }
            hasPlans {
                adding.belief {
                    this.takeIf { it == "testBelief" }
                } triggers {
                    agent.print("Belief added: $context")
                    skills.terminate()
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
