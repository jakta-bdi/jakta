package it.unibo.jakta.dsl.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.node.BaseNode

/**
 * Helper object that provides a convenient way to create a [BaseNodeBuilder] for [BaseNodeBuilder] instances.
 */
object NodeBuilders {
    /**
     * Creates a [BaseNodeBuilder] for an [it.unibo.jakta.node.BaseNode] with the specified [Body] type.
     * When [Body] is [Any], agents need no `embodiedAs`: see [defaultBody].
     */
    inline fun <reified Body : Any> baseNode(): () -> BaseNodeBuilder<Body, BaseNode<Body>> = {
        BaseNodeBuilder<Body, BaseNode<Body>>(BaseNode(), defaultBody<Body>())
    }

    /**
     * The body of agents that do not call `embodiedAs`, on a node whose body type is [Body]:
     * a new [Any] for each agent when [Body] is [Any], none otherwise (there is no body to choose for them).
     */
    @Suppress("UNCHECKED_CAST") // Body is Any
    inline fun <reified Body : Any> defaultBody(): ((AgentID) -> Body)? =
        if (Body::class == Any::class) { _ -> Any() as Body } else null
}
