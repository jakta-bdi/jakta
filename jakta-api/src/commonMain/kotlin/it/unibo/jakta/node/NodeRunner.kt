package it.unibo.jakta.node

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Represents a Node of a Multi-Agent System hosting a set of agents operating.
 */
interface NodeRunner<N : ExecutableNode<*>> {

    /**
     * The set of nodes that this runner is responsible for managing and executing.
     */
    val nodes: Set<N>

    /**
     * Runs the specified [node] on this runner.
     */
    suspend fun run(node: N)

    /**
     * Runs all the [nodes] of a MAS, returning when all of them have terminated.
     * By default, it runs each of them concurrently with [run];
     * runners that need to know all the nodes before starting them can override it.
     */
    suspend fun runAll(nodes: Collection<N>): Unit = supervisorScope {
        nodes.forEach { launch { run(it) } }
    }
}
