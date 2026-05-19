package it.unibo.jakta.dsl

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas.containsBeliefMatching
import it.unibo.jakta.dsl.mas.ifGoalMatches
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestStringIncarnationFunctions {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    private fun runMas(block: PlanLibraryBuilder<String, String, TerminationSkill>.() -> Unit) {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        agent("HelloAgent") {
                            embodiedAs { object {} }
                            withSkills { TerminationSkill(it) }
                            believes {
                                +"goal"
                            }
                            hasInitialGoals {
                                !"goal"
                            }
                            hasPlans(block)
                        }
                    }
                }.run(CoroutineNodeRunner())
            }
            job.join()
        }
    }

    @Test
    fun `test that string Goal triggers a plan and matches a Belief Query`() {
        runMas {
            adding.goal {
                ifGoalMatches("goal")
            } onlyWhen {
                this.beliefs.containsBeliefMatching("goal")
            } triggers {
                agent.print("Hello World!")
                skills.terminateNode()
            }
        }
    }

    @Test
    fun `test that string Goal failure a plan and matches a Belief Query`() {
        runMas {
            adding.goal {
                ifGoalMatches("goal")
            } onlyWhen {
                this.beliefs.containsBeliefMatching("non-existing-belief")
            } triggers {
                fail()
                Unit
            }
            failing.goal {
                ifGoalMatches("goal")
            } triggers {
                agent.print("Hello World!")
                skills.terminateNode()
            }
        }
    }

    @Test
    fun `test that string Belief triggers a plan and matches a Belief Query`() {
        runMas {
            adding.belief {
                ifGoalMatches("goal")
            } onlyWhen {
                this.beliefs.containsBeliefMatching("goal")
            } triggers {
                agent.print("Hello World!")
                skills.terminateNode()
            }
        }
    }
}
