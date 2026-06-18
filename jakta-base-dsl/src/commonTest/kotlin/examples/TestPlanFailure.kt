package examples

import NodeTerminationSkill
import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import terminateNode

class TestPlanFailure {

    @Test
    fun testPlanFailureHandling() {
        Logger.setMinSeverity(Severity.Warn)
        executeInTestScope<Any> {
            node {
                context(NodeTerminationSkillImpl(node)) {
                    agent {
                        embodiedAs { object {} }

                        hasInitialGoals { !"goalChain" }
                        hasPlans {
                            adding.goal {
                                ifGoalMatch("goalChain")
                            } triggers {
                                val x: Unit = agent.achieve("failingPlan")
                                agent.print("The plan has failed but recovered")
                                terminateNode()
                            }
                            adding.goal {
                                ifGoalMatch("failingPlan")
                            } triggers {
                                throw Exception("This plan is meant to fail")
                                42
                            }
                            failing.goal {
                                ifGoalMatch("goalChain")
                            } triggers {
                                agent.print("Goal chain failed as expected.")
                                terminateNode()
                            }
//                            failing.goal {
//                                ifGoalMatch("failingPlan")
//                            } triggers {
//                                agent.print("Plan failed as expected.")
//                            }
                        }
                    }
                }
            }
        }
    }
}
