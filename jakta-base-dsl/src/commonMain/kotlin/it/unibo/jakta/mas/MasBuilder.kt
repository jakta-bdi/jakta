package it.unibo.jakta.mas

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeBuilder
import it.unibo.jakta.node.NodeRunner

/**
 * A builder for a Multi-Agent System (MAS). It allows defining nodes and running them with a given runner.
 */
interface MasBuilder<N : Node<*, *>, NB : NodeBuilder<*, *, *, *, N>> {

    /**
     * Opens a scope for adding a node to the MAS.
     */
    fun node(block: NB.() -> Unit)

    /**
     * Runs the MAS using the provided [NodeRunner]. This will execute all the nodes defined in the MAS.
     *
     * @param runner The [NodeRunner] to use for running the nodes.
     */
    suspend fun run(runner: NodeRunner<N>)
}

/**
 * DSL entrypoint for creating a Multi-Agent System (MAS).
 * It takes a [NodeBuilder] and a block of code that defines the MAS structure.
 */
@JaktaDSL
fun <N : Node<*, *>, NB : NodeBuilder<*, *, *, *, N>> mas(
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
