package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeRunner
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Base implementation of a MasBuilder, with a strategy to build nodes and run them concurrently.
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

    override suspend fun run(runner: NodeRunner<N>) {
        supervisorScope {
            nodes.forEach {
                launch {
                    runner.run(it)
                }
            }
        }
    }
}
