package it.unibo.jakta.dsl

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.mas.MasBuilder
import it.unibo.jakta.node.LocalNode
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeBuilder
import it.unibo.jakta.node.NodeRunner

class RuntimeNodes<N: Node<*,*>>(
    val nodes: Set<N>
)

class DeviceBuilder<N: Node<*, *>, NB: NodeBuilder<*, *, *, *, N>>(val builder: NB) {

    val nodes = mutableSetOf<N>()

    fun node(block: NB.() -> Unit) {
        nodes += builder.apply(block).build()
    }
}

@JaktaDSL
fun <N: Node<*, *>, NB: NodeBuilder<*, *, *, *, N>> device (builder: NB, block: DeviceBuilder<N, NB>.() -> Unit): RuntimeNodes<N> {
     return RuntimeNodes(DeviceBuilder(builder).apply(block).nodes)
}
