package it.unibo.jakta

import it.unibo.jakta.agent.achieve
import it.unibo.jakta.belief.planIsApplicableFor
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.prologGoal
import it.unibo.jakta.goal.ifGoalMatches
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.tuprolog.core.Var
import kotlin.test.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestPrologIncarnation {

    @Test
    fun `test prolog recursion`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        agent("Alice") {
                            embodiedAs { object {} }
                            withSkills { object {} }
                            hasInitialGoals {
                                !prologGoal { "start"(0, 10) }
                            }
                            hasPlans {
                                val myVar: Var = Var.of("X")
                                val upper: Var = Var.of("M")
                                val esse: Var = Var.of("S")
                                adding.goal {
                                    ifGoalMatches(prologGoal { "start"(myVar, myVar) })
                                } triggers {
                                    val terms = this.context[myVar]
                                    agent.print(terms.toString())
                                }

                                adding.goal {
                                    ifGoalMatches(prologGoal { "start"(myVar, upper) })
                                } onlyWhen {
                                    planIsApplicableFor {
                                        (myVar lowerThan upper) and ( esse `is` (myVar + 1))
                                    }
                                } triggers {
                                    println("TEST: " + this.context[myVar])
                                    println("Context received from Guard: ${this.context}")
                                    val newGoal = prologGoal { "start"(this.context[esse]!!, this.context[upper]!!) }
                                    val pippo: Unit = agent.achieve(newGoal)
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
