package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.belief.condition
import it.unibo.jakta.dsl.JaktaLogicProgrammingScope
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.goal
import it.unibo.jakta.dsl.goalQuery
import it.unibo.jakta.goal.asInt
import it.unibo.jakta.goal.matching
import it.unibo.jakta.goal.value
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestPrologIncarnation {


    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun `test prolog recursion`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        agent("Alice") {
                            embodiedAs { object {} }
                            withSkills { TerminationSkill(it) }
                            hasInitialGoals {
                                with(JaktaLogicProgrammingScope()) {
                                    ! goal { "start"(0, 10) }
                                }
                            }
                            hasPlans {
                                with(JaktaLogicProgrammingScope()){
                                    adding.goal {
                                        matching(goalQuery { "start"(N, N) })
                                    } triggers {
                                        with(this.context) {
                                            val n = N.value!!.asInt()
                                            agent.print("Counting...$n done!")
                                        }
                                    }
                                }

                                with(JaktaLogicProgrammingScope()) {
                                    adding.goal {
                                        matching(goalQuery { "start"(N, X) })
                                    } onlyWhen {
                                        condition {
                                            (N lowerThan X) and (S `is` (N + 1))
                                        }
                                    } triggers {
                                        with(this.context) {
                                            agent.print("Counting..." + N.value?.asInt())
                                            val pippo: Unit = agent.achieve(goal { "start"(S.value?.term!!, X.value?.term!!) })
                                            assert(true)
                                            skills.terminateNode()
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
