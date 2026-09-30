package it.unibo.jakta.artifact

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class TestArtifactThreads {

    @Test
    fun operationsAreAtomicWhenAgentsRunInParallel() {
        Logger.setMinSeverity(Severity.Error)
        val agents = 4
        val increments = 250
        val returned = ConcurrentHashMap.newKeySet<Int>()
        val threads = ConcurrentHashMap.newKeySet<String>()
        Executors.newFixedThreadPool(agents).asCoroutineDispatcher().use { pool ->
            runBlocking(pool) {
                withTimeout(TIMEOUT) {
                    mas(NodeBuilders.baseNode<Any>()) {
                        node {
                            val counter = node.makeArtifact(Counter("counter"))
                            repeat(agents) { i ->
                                agent<String, String>(BaseAgentID("user$i")) {
                                    embodiedAs { Any() }
                                    hasInitialGoals { !"use" }
                                    hasPlanLibrary {
                                        adding.goal { ifGoalMatch("use") } triggers {
                                            repeat(increments) {
                                                threads += Thread.currentThread().name.substringBefore(" @")
                                                val count = counter.inc()
                                                returned += count
                                                if (count == agents * increments) node.terminateNode()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
                }
            }
        }
        // no lost or duplicated increments: every invocation saw a different count
        assertEquals((1..agents * increments).toSet(), returned)
        println("Agents ran on ${threads.size} threads")
    }

    private companion object {
        const val TIMEOUT = 20_000L
    }
}
