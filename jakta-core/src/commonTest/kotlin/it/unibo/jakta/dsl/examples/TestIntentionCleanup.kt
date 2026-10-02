package it.unibo.jakta.dsl.examples

import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.runToEnd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

/**
 * Tests of how intentions and plans are cleaned up when they complete, or when the agent stops.
 */
class TestIntentionCleanup {

    /**
     * Intentions leave the pool once their plans have completed.
     */
    @Test
    fun completedIntentionsAreRemoved() = runTest {
        var intentions = -1
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"a"
                    !"b"
                }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("a") } triggers { }
                    adding.goal { ifGoalMatch("b") } triggers {
                        delay(1.seconds)
                        intentions = agent.intentions.size
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(1, intentions)
    }

    /**
     * Stopping an agent cancels its plans, which run their finally blocks but no removal plan.
     */
    @Test
    fun plansOfAStoppedAgentAreCancelled() = runTest {
        val trace = mutableListOf<String>()
        node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"long"
                    !"stop"
                }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("long") } triggers {
                        try {
                            delay(10.seconds)
                            trace += "long completed"
                        } finally {
                            trace += "long cleanup"
                        }
                    }
                    // the agent stopping is not an intentional removal of its goals
                    removing.goal { ifGoalMatch("long") } triggers { trace += "long removed" }
                    adding.goal { ifGoalMatch("stop") } triggers {
                        delay(1.seconds)
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("long cleanup"), trace)
    }

    /**
     * Removing an agent cancels its plans: a chain of subgoals unwinds from the innermost one, like nested calls,
     * even when a cleanup suspends. No failure or removal plan is triggered.
     */
    @Test
    fun plansOfARemovedAgentAreCancelledFromTheInnermost() = runTest {
        val trace = mutableListOf<String>()
        val aliceID = BaseAgentID("Alice")
        node(NodeBuilders.baseNode()) {
            agent(aliceID) {
                embodiedAs { Any() }
                hasInitialGoals { !"work" }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("work") } triggers {
                        try {
                            agent.achieve("subgoal")
                            trace += "work completed"
                        } finally {
                            trace += "work cleanup"
                        }
                    }
                    adding.goal { ifGoalMatch("subgoal") } triggers {
                        try {
                            delay(10.seconds)
                            trace += "subgoal completed"
                        } finally {
                            withContext(NonCancellable) { delay(1.seconds) }
                            trace += "subgoal cleanup"
                        }
                    }
                    failing.goal { ifGoalMatch("work") } triggers { trace += "work failed" }
                    removing.goal { ifGoalMatch("work") } triggers { trace += "work removed" }
                    removing.goal { ifGoalMatch("subgoal") } triggers { trace += "subgoal removed" }
                }
            }
            agent {
                embodiedAs { Any() }
                hasInitialGoals { !"remove" }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("remove") } triggers {
                        delay(1.seconds)
                        node.removeAgent(aliceID)
                        delay(20.seconds)
                        trace += "done"
                        node.terminateNode()
                    }
                }
            }
        }.runToEnd()
        assertEquals(listOf("subgoal cleanup", "work cleanup", "done"), trace)
    }
}
