package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.NodeTerminationSkill
import it.unibo.jakta.dsl.NodeTerminationSkillImpl
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import kotlin.test.Test

class TestPlanFailure {

    @Test
    fun testPlanFailureHandling() {
        Logger.setMinSeverity(Severity.Warn)
        executeInTestScope<Any, NodeTerminationSkill> {
            node {
                agent {
                    embodiedAs { object {} }
                    withSkills { NodeTerminationSkillImpl(it) }
                    hasInitialGoals { !"goalChain" }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("goalChain")
                        } triggers {
                            val x: Unit = agent.achieve("failingPlan")
                            agent.print("The plan has failed but recovered")
                            skills.terminateNode()
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
                            skills.terminateNode()
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
