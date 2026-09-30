package it.unibo.jakta.dsl

import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.JaktaForAlchemistNode
import it.unibo.jakta.node.RuntimeNodes

/**
 * Factory method for building a [JaktaForAlchemistNode].
 * @return a factory for a [BaseNodeBuilder] which creates a [JaktaForAlchemistNode].
 */
fun <Body : Any> NodeBuilders.alchemistNode(): () -> BaseNodeBuilder<Body, JaktaForAlchemistNode<Body>> = {
    BaseNodeBuilder<Body, JaktaForAlchemistNode<Body>>(JaktaForAlchemistNode())
}

/**
 * Builder of a device inside the simulation.
 * A device corresponds to a simulated node, which inside can host one or more JaKtA nodes.
 * @param builderFactory the factory of the NodeBuilder used for each JaKtA node.
 */
class DeviceBuilder<N : ExecutableNode<*>, NB : NodeBuilder<*, N>>(val builderFactory: () -> NB) {

    /**
     * JaKtA nodes executing inside of this alchemist node.
     */
    val nodes = mutableSetOf<N>()

    /**
     * Defines a new JaKtA node to be executed inside of the Alchemist node.
     */
    fun node(block: NB.() -> Unit) {
        nodes += builderFactory().apply(block).build()
    }
}

/**
 * Device entrypoint for the simulation custom DSL.
 */
@JaktaDSL
fun <N : ExecutableNode<*>, NB : NodeBuilder<*, N>> device(
    builderFactory: () -> NB,
    block: DeviceBuilder<N, NB>.() -> Unit,
): RuntimeNodes<N> = RuntimeNodes(DeviceBuilder(builderFactory).apply(block).nodes)
