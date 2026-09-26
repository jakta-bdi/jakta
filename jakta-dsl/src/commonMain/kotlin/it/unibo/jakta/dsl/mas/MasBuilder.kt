package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.node.NodeRunner

/**
 * A builder for a Multi-Agent System (MAS). It allows defining nodes and running them with a given runner.
 */
interface MasBuilder<N : ExecutableNode<*>, NB : NodeBuilder<*, N>> {

    /**
     * The nodes of the MAS.
     */
    val nodes: List<N>

    /**
     * Opens a scope for adding a node to the MAS.
     */
    fun node(block: NB.() -> Unit)

    /**
     * Opens a scope for adding a node with the given [id] to the MAS.
     * Explicit ids are required to run the nodes of a MAS in different processes.
     */
    fun node(id: NodeID, block: NB.() -> Unit)

    /**
     * Adds nodes to the mas.
     */
    fun withNodes(vararg node: N)

    /**
     * Runs the MAS using the provided [NodeRunner]. This will execute all the nodes defined in the MAS.
     *
     * @param runner The [NodeRunner] to use for running the nodes.
     */
    suspend fun run(runner: NodeRunner<N>)
}
