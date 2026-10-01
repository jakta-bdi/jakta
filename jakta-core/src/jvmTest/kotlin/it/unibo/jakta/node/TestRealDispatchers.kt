package it.unibo.jakta.node

import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Agents must run on any dispatcher, including those that do not implement `kotlinx.coroutines.Delay`.
 */
@Suppress("InjectDispatcher") // the dispatchers under test are the point of this test
class TestRealDispatchers {
    private fun runDelayingAgentOn(dispatcher: CoroutineDispatcher) = runBlocking(dispatcher) {
        val node = node(NodeBuilders.baseNode()) {
            agent {
                embodiedAs { Any() }
                hasInitialGoals { !"goal" }
                hasPlanLibrary {
                    adding.goal { ifGoalMatch("goal") } triggers {
                        delay(50.milliseconds)
                        node.terminateNode()
                    }
                }
            }
        }
        withTimeout(5.seconds) {
            CoroutineNodeRunner<Any, ExecutableNode<Any>>(SharedMemoryNetwork()).run(node)
        }
    }

    @Test
    fun agentRunsOnDefaultDispatcher() = runDelayingAgentOn(Dispatchers.Default)

    @Test
    fun agentRunsOnIODispatcher() = runDelayingAgentOn(Dispatchers.IO)

    @Test
    fun agentRunsOnExecutorDispatcher() =
        Executors.newFixedThreadPool(2).asCoroutineDispatcher().use { runDelayingAgentOn(it) }
}
