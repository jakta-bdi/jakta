package it.unibo.jakta.dsl

import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.Node

/**
 * Custom Runtime for Alchemist.
 * It serves only as a JaKtA node container.
 */
class RuntimeNodes<N : Node<*>>(
    /**
     * The JaKtA nodes that are being executed in the node.
     */
    val nodes: Set<N>,
)

/**
 * Builder of a device inside the simulation.
 * A device corresponds to a simulated node, which inside can host one or more JaKtA nodes.
 * @param builder the NodeBuilder instance.
 */
class DeviceBuilder<N : Node<*>, NB : NodeBuilder<*, N>>(val builder: NB) {

    /**
     * JaKtA nodes executing inside of this alchemist node.
     */
    val nodes = mutableSetOf<N>()

    /**
     * Defines a new JaKtA node to be executed inside of the Alchemist node.
     */
    fun node(block: NB.() -> Unit) {
        nodes += builder.apply(block).build()
    }
}

/**
 * Device entrypoint for the simulation custom DSL.
 */
@JaktaDSL
fun <N : Node<*>, NB : NodeBuilder<*, N>> device(
    builder: NB,
    block: DeviceBuilder<N, NB>.() -> Unit,
): RuntimeNodes<N> = RuntimeNodes(DeviceBuilder(builder).apply(block).nodes)
