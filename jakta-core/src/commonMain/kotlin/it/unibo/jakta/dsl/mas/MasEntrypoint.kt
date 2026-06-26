package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeRunner

/**
 * DSL entrypoint for creating a Multi-Agent System (MAS).
 * It takes a [NodeBuilder] and a block of code that defines the MAS structure.
 */
@JaktaDSL
fun <N : Node<*>, NB : NodeBuilder<*, N>> mas(
    builder: NB,
    block: MasBuilder<N, NB>.() -> Unit,
): MasBuilder<N, NB> = object : MasBuilder<N, NB> {

    val nodes = mutableListOf<N>()

    override fun node(block: NB.() -> Unit) {
        nodes += builder.apply(block).build()
    }

    override suspend fun run(runner: NodeRunner<N>) {
        nodes.forEach { runner.run(it) }
    }
}.apply(block)
