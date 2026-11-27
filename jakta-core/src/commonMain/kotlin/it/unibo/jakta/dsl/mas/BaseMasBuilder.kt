package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeRunner

/**
 * Base implementation of a MasBuilder, with a strategy to build nodes, run with [NodeRunner.runAll].
 * @param builderFactory the strategy factory to create new node builders.
 */
class BaseMasBuilder<N : ExecutableNode<*>, NB : NodeBuilder<*, N>>(val builderFactory: () -> NB) : MasBuilder<N, NB> {

    private val nodes = mutableListOf<N>()

    override fun node(block: NB.() -> Unit) {
        nodes += builderFactory().apply(block).build()
    }

    override fun withNodes(vararg node: N) {
        nodes += node
    }

    override suspend fun run(runner: NodeRunner<N>) = runner.runAll(nodes)
}
