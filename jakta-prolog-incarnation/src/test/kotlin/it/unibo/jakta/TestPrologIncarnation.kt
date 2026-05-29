package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.beliefQuery
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matching
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.goalQuery
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.condition
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
                                !initialGoal { "start"(0, 10) }
                            }
                            hasPlans {
                                prologPlan {
                                    adding.goal {
                                        matching { "start"(N, N) }
                                    } triggers {
                                        val n = N.valueFromContext(context)
                                        agent.print("Counting...$n done!")
                                    }
                                }
                                prologPlan {
                                    adding.goal {
                                        matching { "start"(N, X) }
                                    } onlyWhen {
                                        condition {
                                            (N lowerThan X) and (S `is` (N + 1))
                                        }
                                    } triggers {
                                        with(this.context) {
                                            agent.print("Counting..." + N.value)
                                            agent.achieve(goal { "start"(S.value, X.value) })
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

    @Test
    fun `test prolog belief plan`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        agent("Alice") {
                            embodiedAs { object {} }
                            withSkills { TerminationSkill(it) }
                            believes {
                                +initialBelief { "belief"(1) }
                            }
                            hasPlans {
                                prologPlan {
                                    adding.belief {
                                        matching { "belief"(N) }
                                    } triggers {
                                        val n = N.valueFromContext(context)
                                        agent.print("Belief is $n")
                                        skills.terminateNode()
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

    @Test
    fun `test prolog belief update`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        agent("Alice") {
                            embodiedAs { object {} }
                            withSkills { TerminationSkill(it) }
                            believes {
                                +initialBelief { "belief"(1) }
                            }
                            hasPlans {
                                prologPlan {
                                    adding.belief {
                                        matching { "belief"(N) }
                                    } onlyWhen {
                                        condition { N greaterThan 5 }
                                    } triggers {
                                        skills.terminateNode()
                                    }
                                }

                                prologPlan {
                                    adding.belief {
                                        matching { "belief"(N) }
                                    } triggers {
                                        with(context) {
                                            val n = N.value.getAs<Int>()
                                            agent.print("Belief is $n")
                                            delay(1.milliseconds) // TODO starvation issue if not present...
                                            agent.believe(belief { "belief"(n + 1) })
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
