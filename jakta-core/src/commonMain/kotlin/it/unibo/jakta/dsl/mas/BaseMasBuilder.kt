package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeRunner
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class BaseMasBuilder<N : ExecutableNode<*>, NB : NodeBuilder<*, N>>(
    val builderFactory: () -> NB
) : MasBuilder<N, NB> {

    val nodes = mutableListOf<N>()

    override fun node(block: NB.() -> Unit) {
        nodes += builderFactory().apply(block).build()
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
