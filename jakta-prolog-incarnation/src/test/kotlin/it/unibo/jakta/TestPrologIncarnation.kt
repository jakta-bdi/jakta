package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.belief.belief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.goal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.achieve
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
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
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !initialGoal { "start"(0, 10) }
                            }
                            hasPlanLibrary {
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(N, N) }
                                    } triggers {
                                        agent.print("Counting...", N, " done!")
                                    }
                                }
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(N, X) }
                                    } onlyWhen {
                                        satisfies {
                                            (N lowerThan X) and (S `is` (N + 1))
                                        }
                                    } triggers {
                                        agent.print("Counting...", N)
                                        agent.achieve(goal { "start"(S, X) })
                                        assert(true)
                                        node.terminateNode()
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            }
            job.join()
        }
    }

    @Test
    fun `test prolog belief plan`() {
        runTest {
            val job = launch {
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !initialGoal { "start"(1) }
                            }
                            hasPlanLibrary {
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(N) }
                                    } triggers {
                                        agent.print("Starting with ", N)
                                        agent.believe(belief { "belief"(N) })
                                    }
                                }

                                prologPlan {
                                    adding.belief {
                                        matchingBelief { "belief"(N) }
                                    } triggers {
                                        agent.print("Belief is ", N)
                                        node.terminateNode()
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            }
            job.join()
        }
    }

    @Test
    fun `test prolog belief update`() {
        runTest {
            val job = launch {
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !initialGoal { "start"(1) }
                            }
                            hasPlanLibrary {
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(N) }
                                    } triggers {
                                        agent.print("Starting with ", N)
                                        agent.believe(belief { "belief"(N) })
                                    }
                                }

                                prologPlan {
                                    adding.belief {
                                        matchingBelief { "belief"(N) }
                                    } onlyWhen {
                                        satisfies { N greaterThan 5 }
                                    } triggers {
                                        node.terminateNode()
                                    }
                                }

                                prologPlan {
                                    adding.belief {
                                        matchingBelief { "belief"(N) }
                                    } triggers {
                                        val n = N.value<Int>()
                                        agent.print("Belief is $n")
                                        agent.believe(belief { "belief"(n + 1) })
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            }
            job.join()
        }
    }

    @Test
    fun `test belief matching in guards`() {
        runTest {
            val job = launch {
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
                            embodiedAs { Any() }
                            believes {
                                +initialBelief { "belief"(1) }
                            }
                            hasInitialGoals {
                                !initialGoal { "start"(1) }
                            }
                            hasPlanLibrary {
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(`_`) }
                                    } onlyWhen {
                                        satisfies { "belief"(N) }
                                    } triggers {
                                        agent.print("Belief is ", N)
                                        node.terminateNode()
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            }
            job.join()
        }
    }

    @Test
    fun `test inference rule`() {
        runTest {
            val job = launch {
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
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
                            hasPlanLibrary {
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(B) }
                                    } onlyWhen {
                                        satisfies { "sibling"(B, C) }
                                    } triggers {
                                        agent.print(C, " is a sibling of ",B)
                                        node.terminateNode()
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
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
                mas(NodeBuilders.baseNode()) {
                    node {
                        agent {
                            embodiedAs { Any() }
                            believes {
                                +initialBelief { "parent"("alice", "bob") }
                                +initialBelief { "parent"("alice", "charlie") }
                            }
                            hasInitialGoals {
                                !initialGoal { "start"("bob") }
                            }
                            hasPlanLibrary {
                                prologPlan {
                                    adding.goal {
                                        matchingGoal { "start"(B) }
                                    } onlyWhen {
                                        satisfies { "sibling"(B, C) }
                                    } triggers {
                                        agent.print(C, " is a sibling of ",B)
                                        node.terminateNode()
                                    }
                                }

                                prologPlan {
                                    failing.goal {
                                        matchingGoal { "start"(B) }
                                    } triggers {
                                        agent.print("I didn't know how to infer siblings for ", B)
                                        agent.believe(rule)
                                        agent.print("But now I do! I can try again...")
                                        agent.alsoAchieve(goal { "start"(B) })
                                    }
                                }
                            }
                        }
                    }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            }
            job.join()
        }
    }
}
