package it.unibo.jakta.dsl.node

import it.unibo.jakta.node.BaseNode

/**
 * Helper object that provides a convenient way to create a [BaseNodeBuilder] for [BaseNodeBuilder] instances.
 */
object NodeBuilders {
    /**
     * Creates a [BaseNodeBuilder] for an [it.unibo.jakta.node.BaseNode] with the specified [Body] type.
     */
    fun <Body : Any> baseNode() = BaseNodeBuilder<Body, BaseNode<Body>>(nodeFactory = { BaseNode() })
}
