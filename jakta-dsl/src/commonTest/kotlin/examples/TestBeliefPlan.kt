package examples

import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import it.unibo.jakta.mas.mas
import it.unibo.jakta.node
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.node.baseImpl.CoroutineNodeRunner
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TestBeliefPlan {

    val helloWorld = node {
        agent("Hello world agent") {
            body = object {}
            withSkills { NodeTerminationSkillImpl(this@node.node) }
            believes {
                +"testBelief"
            }
            hasPlans {
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
