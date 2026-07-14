package it.unibo.jakta.dsl

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.belief.containsBeliefMatching
import it.unibo.jakta.belief.ifGoalMatches
import it.unibo.jakta.belief.matchesRegex
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNodeConnection
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TestStringIncarnationFunctions {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Error)
    }

    private fun fail(message: String = ""): Unit = kotlin.test.fail(message)

    private fun runMas(block: context(NodeTerminationSkill) PlanLibraryBuilder<String, String>.() -> Unit) {
        runTest {
            val job = launch {
                mas(NodeBuilders.baseNode()) {
                    node {
                        context(NodeTerminationSkill(node)) {
                            agent {
                                embodiedAs { Any() }
                                believes {
                                    +"goal"
                                }
                                hasInitialGoals {
                                    !"goal"
                                }
                                hasPlans {
                                    block()
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(LocalNodeConnection()))
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
                containsBeliefMatching("goal")
            } triggers {
                agent.print("Hello World!")
                terminateNode()
            }
        }
    }

    @Test
    fun `test that string Goal failure a plan and matches a Belief Query`() {
        runMas {
            adding.goal {
                ifGoalMatches("goal")
            } onlyWhen {
                containsBeliefMatching("non-existing-belief")
            } triggers {
                fail()
            }
            failing.goal {
                ifGoalMatches("goal")
            } triggers {
                agent.print("Hello World!")
                terminateNode()
            }
        }
    }

    @Test
    fun `test that string Belief triggers a plan and matches a Belief Query`() {
        runMas {
            adding.goal {
                ifGoalMatches("goal")
            } triggers {
                agent.believe("belief")
            }
            adding.belief {
                ifGoalMatches("belief")
            } triggers {
                agent.print("Hello World!")
                terminateNode()
            }
        }
    }

    @Test
    fun `test trigger using regex`() {
        runMas {
            adding.goal {
                matchesRegex("g+")
            } onlyWhen {
                containsBeliefMatching("goal")
            } triggers {
                agent.print("Hello World!")
                terminateNode()
            }
        }
    }
}
