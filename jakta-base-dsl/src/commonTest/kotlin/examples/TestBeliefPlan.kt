package examples

import NodeTerminationSkill
import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import it.unibo.jakta.node
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.plan.PlanLibraryBuilder
import it.unibo.jakta.plan.PlanScope
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestBeliefPlan {

    context(terminator: NodeTerminationSkill)
    fun PlanLibraryBuilder<String, *>.terminateOn(test: String) = adding.belief {
            this.takeIf { it == test }
        } triggers {
            agent.print("Belief added: $context")
            terminator.terminateNode()
        }

    context(terminator: NodeTerminationSkill)
    fun <Goal : Any> LocalNodeBuilder<String, Goal, Any>.testAgent() =
        agent("Hello world agent") {
            embodiedAs { Any() }
            believes {
                +"testBelief"
            }
            hasPlans {
                terminateOn("testBelief")
            }
        }

    val helloWorld = node {
        context(NodeTerminationSkillImpl()) {
            testAgent<String>()
        }
    }

//
//        agent("Hello world agent") {
//            embodiedAs { object {} }
//            withSkills { NodeTerminationSkillImpl(it) }
//            believes {
//                +"testBelief"
//            }
//            hasPlans {
//                adding.belief {
//                    this.takeIf { it == "testBelief" }
//                } triggers {
//                    agent.print("Belief added: $context")
//                    skills.terminateNode()
//                }
//            }
//        }
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
