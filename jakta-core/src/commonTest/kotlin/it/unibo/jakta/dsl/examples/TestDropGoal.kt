package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.GoalDroppedException
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.runToEnd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class TestDropGoal {

    @Test
    fun droppingAGoalCancelsItsWholeIntention() = runTest {
        Logger.setMinSeverity(Severity.Warn)
        val trace = mutableListOf<String>()
        val cleanups = mutableSetOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"work"
                    !"drop"
                }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("work") } triggers {
                        try {
                            agent.achieve("subgoal")
                            trace += "work completed"
                        } finally {
                            cleanups += "work"
                        }
                    }
                    adding.goal { ifGoalMatch("subgoal") } triggers {
                        try {
                            delay(10.seconds)
                            trace += "subgoal completed"
                        } finally {
                            cleanups += "subgoal"
                        }
                    }
                    failing.goal { ifGoalMatch("work") } triggers {
                        trace += "work failed"
                    }
                    removing.goal { ifGoalMatch("work") } triggers {
                        trace += "work removed"
                    }
                    adding.goal { ifGoalMatch("drop") } triggers {
                        delay(1.seconds)
                        trace += "goals before drop: ${agent.goals.sorted()}"
                        agent.dropGoal("work")
                        delay(1.seconds)
                        trace += "goals after drop: ${agent.goals.sorted()}"
                        trace += "intentions after drop: ${agent.intentions.size}"
                        trace += "cleanups after drop: ${cleanups.sorted()}"
                        delay(20.seconds)
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(
            listOf(
                "goals before drop: [drop, subgoal, work]",
                "work removed",
                "goals after drop: [drop]",
                "intentions after drop: 1",
                "cleanups after drop: [subgoal, work]",
            ),
            trace,
        )
    }

    @Test
    fun droppingASubgoalFailsTheParent() = runTest {
        Logger.setMinSeverity(Severity.Warn)
        val trace = mutableListOf<String>()
        val cleanups = mutableSetOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"work"
                    !"drop"
                }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("work") } triggers {
                        agent.achieve("subgoal")
                        trace += "work completed"
                    }
                    adding.goal { ifGoalMatch("subgoal") } triggers {
                        try {
                            agent.achieve("nested")
                            trace += "subgoal completed"
                        } finally {
                            cleanups += "subgoal"
                        }
                    }
                    adding.goal { ifGoalMatch("nested") } triggers {
                        try {
                            delay(10.seconds)
                            trace += "nested completed"
                        } finally {
                            cleanups += "nested"
                        }
                    }
                    failing.goal { ifGoalMatch("work") } triggers {
                        trace += "work failed"
                    }
                    adding.goal { ifGoalMatch("drop") } triggers {
                        delay(1.seconds)
                        agent.dropGoal("subgoal")
                        delay(20.seconds)
                        trace += "goals: ${agent.goals.sorted()}"
                        trace += "cleanups: ${cleanups.sorted()}"
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("work failed", "goals: [drop]", "cleanups: [nested, subgoal]"), trace)
    }

    @Test
    fun theParentCanRecoverFromADroppedSubgoal() = runTest {
        Logger.setMinSeverity(Severity.Warn)
        val trace = mutableListOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals { !"work" }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("work") } triggers {
                        try {
                            agent.achieve("subgoal")
                        } catch (e: GoalDroppedException) {
                            trace += "${e.goal} dropped"
                        }
                        node.terminateNode()
                    }
                    adding.goal { ifGoalMatch("subgoal") } triggers {
                        agent.dropGoal("subgoal")
                        delay(10.seconds)
                        trace += "subgoal completed"
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("subgoal dropped"), trace)
    }
}
