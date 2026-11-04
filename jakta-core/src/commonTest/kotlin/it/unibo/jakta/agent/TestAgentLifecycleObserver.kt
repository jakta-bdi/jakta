package it.unibo.jakta.agent

import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestAgentLifecycleObserver {

    @Test
    fun theObserverSeesEveryHandledEventAfterItWasHandled(): TestResult = runTest {
        val observed = mutableListOf<Pair<AgentEvent, Collection<Any>>>()
        val alice = BaseAgentID("alice")
        mas(NodeBuilders.baseNode()) {
            node {
                agent<String, String>(alice) {
                    embodiedAs { Any() }
                    hasInitialGoals { !"greet" }
                    hasPlanLibrary {
                        adding.goal { ifGoalMatch("greet") } triggers {
                            agent.believe("greeted")
                        }
                        adding.belief { takeIf { it == "greeted" } } triggers {
                            node.terminateNode()
                        }
                    }
                }
            }
        }.run(
            CoroutineNodeRunner(SharedMemoryNetwork()) { agent, event ->
                assertEquals(alice, agent.id)
                observed += event to agent.state.beliefs
            },
        )
        assertTrue(observed.any { (event, _) -> event is AgentEvent.Internal.Goal.Add<*, *> && event.goal == "greet" })
        val (_, beliefsAfterAddition) = observed.single { (event, _) -> event is AgentEvent.Internal.Belief.Add<*> }
        assertEquals(listOf<Any>("greeted"), beliefsAfterAddition.toList())
    }
}
