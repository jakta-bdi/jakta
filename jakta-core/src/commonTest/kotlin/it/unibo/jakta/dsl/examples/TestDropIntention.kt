package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
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

class TestDropIntention {

    @Test
    fun droppingAnIntentionByOneOfItsGoalsSilentlyCancelsIt() = runTest {
        Logger.setMinSeverity(Severity.Warn)
        val trace = mutableListOf<String>()
        val cleanups = mutableSetOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"work"
                    !"other"
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
                    failing.goal { ifGoalMatch("work") } triggers { trace += "work failed" }
                    removing.goal { ifGoalMatch("work") } triggers { trace += "work removed" }
                    removing.goal { ifGoalMatch("subgoal") } triggers { trace += "subgoal removed" }
                    adding.goal { ifGoalMatch("other") } triggers {
                        delay(5.seconds)
                        trace += "other completed"
                    }
                    adding.goal { ifGoalMatch("drop") } triggers {
                        delay(1.seconds)
                        agent.dropIntention("subgoal")
                        delay(1.seconds)
                        trace += "goals: ${agent.goals.sorted()}"
                        trace += "cleanups: ${cleanups.sorted()}"
                        delay(20.seconds)
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("goals: [drop, other]", "cleanups: [subgoal, work]", "other completed"), trace)
    }

    @Test
    fun droppingAllIntentionsKeepsGoalsNotAdoptedYet() = runTest {
        Logger.setMinSeverity(Severity.Warn)
        val trace = mutableListOf<String>()
        val cleanups = mutableSetOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"a"
                    !"b"
                    !"drop"
                }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("a") } triggers {
                        try {
                            delay(10.seconds)
                        } finally {
                            cleanups += "a"
                        }
                    }
                    adding.goal { ifGoalMatch("b") } triggers {
                        try {
                            delay(10.seconds)
                        } finally {
                            cleanups += "b"
                        }
                    }
                    adding.goal { ifGoalMatch("drop") } triggers {
                        delay(1.seconds)
                        agent.alsoAchieve("after")
                        agent.dropAllIntentions() // including this one
                        try {
                            delay(1.seconds)
                            trace += "drop continued"
                        } finally {
                            cleanups += "drop"
                        }
                    }
                    adding.goal { ifGoalMatch("after") } triggers {
                        delay(1.seconds)
                        trace += "intentions: ${agent.intentions.size}"
                        trace += "cleanups: ${cleanups.sorted()}"
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("intentions: 1", "cleanups: [a, b, drop]"), trace)
    }
}
