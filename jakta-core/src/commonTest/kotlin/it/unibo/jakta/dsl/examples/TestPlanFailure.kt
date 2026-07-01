package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
import kotlin.test.Test

class TestPlanFailure {

    @Test
    fun testPlanFailureHandling() {
        Logger.setMinSeverity(Severity.Warn)
        executeInTestScope {
            node {
                context(BaseNodeTerminationSkill(node)) {
                    agent {
                        embodiedAs { Any() }
                        hasInitialGoals { !"goalChain" }
                        hasPlans {
                            adding.goal {
                                ifGoalMatch("goalChain")
                            } triggers {
                                agent.achieve<String, Unit>("failingPlan")
                                agent.print("The plan has failed but recovered")
                                terminateNode()
                            }
                            adding.goal {
                                ifGoalMatch("failingPlan")
                            } triggers {
                                check(false) { "This plan is meant to fail" }
                                42
                            }
                            failing.goal {
                                ifGoalMatch("goalChain")
                            } triggers {
                                agent.print("Goal chain failed as expected.")
                                terminateNode()
                            }
//                            failing.goal {
//                                it.unibo.jakta.dsl.ifGoalMatch("failingPlan")
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
