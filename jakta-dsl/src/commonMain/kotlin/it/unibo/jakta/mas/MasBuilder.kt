package it.unibo.jakta.mas

import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeBuilder
import it.unibo.jakta.node.NodeRunner

interface MasBuilder<N: Node<*, *>, NB: NodeBuilder<*, *, *, *, N>> {

    fun node(block: NB.() -> Unit)

    suspend fun run(runner: NodeRunner<N>)
}

fun <N: Node<*, *>, NB: NodeBuilder<*, *, *, *, N>> mas (builder: NB, block: MasBuilder<N, NB>.() -> Unit)
    : MasBuilder<N, NB> {
    return object : MasBuilder<N, NB> {

        val nodes = mutableListOf<N>()

        override fun node(block: NB.() -> Unit) {
            nodes += builder.apply(block).build()
        }

        override suspend fun run(runner: NodeRunner<N>) {
            nodes.forEach { runner.run(it) }
        }
    }.apply(block)
}
