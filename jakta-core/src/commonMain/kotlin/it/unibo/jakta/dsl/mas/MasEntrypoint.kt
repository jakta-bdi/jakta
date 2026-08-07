package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeRunner
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * DSL entrypoint for creating a Multi-Agent System (MAS).
 * It takes a [NodeBuilder] and a block of code that defines the MAS structure.
 */
@JaktaDSL
fun <N : ExecutableNode<*>, NB : NodeBuilder<*, N>> mas(
    builderFactory: () -> NB,
    block: MasBuilder<N, NB>.() -> Unit,
): MasBuilder<N, NB> = object : MasBuilder<N, NB> {

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
}.apply(block)
