package it.unibo.jakta.dsl.examples

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

class TestIntentionCleanup {
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
}
