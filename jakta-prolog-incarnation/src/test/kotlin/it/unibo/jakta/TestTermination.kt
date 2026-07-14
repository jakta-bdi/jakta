package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNodeConnection
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
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !initialGoal { "start".toAtom() }
                            }
                            hasPlans {
                                prologPlan {
                                    adding.goal {
                                        matching { "start".toAtom() }
                                    } triggers {
                                        node.terminateNode()
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(LocalNodeConnection()))
            }
            job.join()
        }
    }
}
