package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matching
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matching
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
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
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                hasInitialGoals {
                                    !initialGoal { "start"(0, 10) }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(N, N) }
                                        } triggers {
                                            val n = N.value
                                            agent.print("Counting...$n done!")
                                        }
                                    }
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(N, X) }
                                        } onlyWhen {
                                            satisfies {
                                                (N lowerThan X) and (S `is` (N + 1))
                                            }
                                        } triggers {
                                            agent.print("Counting..." + N.value)
                                            agent.achieve(goal { "start"(S, X) })
                                            assert(true)
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

    @Test
    fun `test prolog belief plan`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                hasInitialGoals {
                                    !initialGoal { "start"(1) }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(N) }
                                        } triggers {
                                            val n = N.value
                                            agent.print("Starting with $n")
                                            agent.believe(belief { "belief"(n) })
                                        }
                                    }

                                    prologPlan {
                                        adding.belief {
                                            matching { "belief"(N) }
                                        } triggers {
                                            val n = N.value
                                            agent.print("Belief is $n")
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

    @Test
    fun `test prolog belief update`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                hasInitialGoals {
                                    !initialGoal { "start"(1) }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(N) }
                                        } triggers {
                                            val n = N.value
                                            agent.print("Starting with $n")
                                            agent.believe(belief { "belief"(n) })
                                        }
                                    }

                                    prologPlan {
                                        adding.belief {
                                            matching { "belief"(N) }
                                        } onlyWhen {
                                            satisfies { N greaterThan 5 }
                                        } triggers {
                                            terminateNode()
                                        }
                                    }

                                    prologPlan {
                                        adding.belief {
                                            matching { "belief"(N) }
                                        } triggers {
                                            val n = N.toKotlin<Int>()
                                            agent.print("Belief is $n")
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

    @Test
    fun `test belief matching in guards`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                believes {
                                    +initialBelief { "belief"(1) }
                                }
                                hasInitialGoals {
                                    !initialGoal { "start"(1) }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(`_`) }
                                        } onlyWhen {
                                            satisfies { "belief"(N) }
                                        } triggers {
                                            agent.print("Belief is ${N.value}")
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

    @Test
    fun `test inference rule`() {
        runTest {
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                believes {
                                    +initialBelief { "parent"("alice", "bob") }
                                    +initialBelief { "parent"("alice", "charlie") }

                                    +inferenceRule {
                                        "sibling"(X, Y) impliedBy (
                                            "parent"(Z, X)
                                                and "parent"(Z, Y)
                                                and (X neq Y)
                                            )
                                    }
                                }
                                hasInitialGoals {
                                    !initialGoal { "start"("bob") }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(B) }
                                        } onlyWhen {
                                            satisfies { "sibling"(B, C) }
                                        } triggers {
                                            agent.print(
                                                "${C.value}" +
                                                    " is a sibling of ${B.value}",
                                            )
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

    @Test
    fun `test dynamic inference rule`() {
        runTest {
            val rule = inferenceRule {
                "sibling"(X, Y) impliedBy (
                    "parent"(Z, X)
                        and "parent"(Z, Y)
                        and (X neq Y)
                    )
            }
            val job = launch {
                mas(LocalNodeBuilder()) {
                    node {
                        context(BaseNodeTerminationSkill(node)) {
                            agent("Alice") {
                                embodiedAs { Any() }
                                believes {
                                    +initialBelief { "parent"("alice", "bob") }
                                    +initialBelief { "parent"("alice", "charlie") }
                                }
                                hasInitialGoals {
                                    !initialGoal { "start"("bob") }
                                }
                                hasPlans {
                                    prologPlan {
                                        adding.goal {
                                            matching { "start"(B) }
                                        } onlyWhen {
                                            satisfies { "sibling"(B, C) }
                                        } triggers {
                                            agent.print(
                                                "${C.value}" +
                                                    " is a sibling of ${B.value}",
                                            )
                                            terminateNode()
                                        }
                                    }

                                    prologPlan {
                                        failing.goal {
                                            matching { "start"(B) }
                                        } triggers {
                                            agent.print("I didn't know how to infer siblings for ${B.value}")
                                            agent.believe(rule)
                                            agent.print("But now I do! I can try again...")
                                            agent.alsoAchieve(goal { "start"(B.value) })
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
