package it.unibo.jakta.node

/**
 * Represents a Node of a Multi-Agent System hosting a set of agents operating.
 */
interface NodeRunner<N : Node<*, *>> {

    /**
     * The set of nodes that this runner is responsible for managing and executing.
     */
    val nodes: Set<N>

    /**
     * Runs the specified [node] on this runner.
     */
    suspend fun run(node: N)
}
