package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matching
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
import it.unibo.tuprolog.core.toAtom
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestTermination {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun `test termination`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                hasInitialGoals {
                                    !initialGoal { "start".toAtom() }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start".toAtom() }
                                        } triggers {
                                            terminateNode()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner())
            }
            job.join()
        }
    }
}
