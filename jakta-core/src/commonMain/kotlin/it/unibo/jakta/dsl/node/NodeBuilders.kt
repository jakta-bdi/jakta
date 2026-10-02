package it.unibo.jakta.dsl.node

import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.node.NodeID

/**
 * Helper object that provides a convenient way to create a [BaseNodeBuilder] for [BaseNodeBuilder] instances.
 */
object NodeBuilders {
    /**
     * Creates a factory of [BaseNodeBuilder]s for [it.unibo.jakta.node.BaseNode]s
     * with the specified [Body] type and the given [NodeID].
     */
    fun <Body : Any> baseNode(): (NodeID) -> BaseNodeBuilder<Body, BaseNode<Body>> = { id ->
        BaseNodeBuilder<Body, BaseNode<Body>>(BaseNode(id))
    }
}
