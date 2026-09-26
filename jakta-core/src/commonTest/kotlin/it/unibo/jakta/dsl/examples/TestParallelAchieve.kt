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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout

/**
 * Tests that a plan pursues its subgoals one at a time.
 */
class TestParallelAchieve {

    /**
     * A plan achieving a goal while it is already achieving another one fails, pointing to alsoAchieve.
     */
    @Test
    fun aPlanCannotAchieveGoalsInParallel() = runTest {
        Logger.setMinSeverity(Severity.Assert)
        val trace = mutableListOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals { !"work" }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("work") } triggers {
                        coroutineScope {
                            launch { agent.achieve("first") }
                            launch { agent.achieve("second") }
                        }
                        trace += "work completed"
                    }
                    adding.goal { ifGoalMatch("first") } triggers { trace += "first started" }
                    adding.goal { ifGoalMatch("second") } triggers { trace += "second started" }
                    failing.goal { ifGoalMatch("work") } triggers {
                        trace += "work failed"
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("first started", "work failed"), trace)
    }

    /**
     * Achieving goals one after the other is allowed, also from nested scopes of the plan.
     */
    @Test
    fun aPlanCanAchieveGoalsOneAtATime() = runTest {
        Logger.setMinSeverity(Severity.Warn)
        val trace = mutableListOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals { !"work" }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("work") } triggers {
                        withTimeout(5.seconds) { agent.achieve("first") }
                        coroutineScope { launch { agent.achieve("second") } }
                        agent.achieve("third")
                        trace += "work completed"
                        node.terminateNode()
                    }
                    for (goal in listOf("first", "second", "third")) {
                        adding.goal { ifGoalMatch(goal) } triggers {
                            delay(1.seconds)
                            trace += "$goal completed"
                        }
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("first completed", "second completed", "third completed", "work completed"), trace)
    }
}
