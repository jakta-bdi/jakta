package it.unibo.jakta.node

import kotlinx.coroutines.Job

/**
 * Represents a Node of a Multi-Agent System hosting a set of agents operating.
 */
interface NodeRunner<N: Node<*, *>> {

    val nodes: Set<N>

    suspend fun run(node: N)
}
