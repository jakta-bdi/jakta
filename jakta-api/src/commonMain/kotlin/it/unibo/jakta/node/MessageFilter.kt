package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentID

/**
 * Selects which agents of a node receive an external event (a message or a perception).
 *
 * The filter is evaluated by the node that delivers the event, once for each of its agents.
 * Filters of messages that cross a network must be serializable data (e.g. a `@Serializable` data class),
 * lambdas only work within a single process.
 * @param Body The type of body of the agents that the filter can evaluate.
 */
fun interface MessageFilter<in Body : Any> {
    /**
     * Returns true if the [agent] hosted by the [node] with the given [body] should receive the event.
     */
    fun accept(node: Node<out Body>, agent: AgentID, body: Body): Boolean
}

/**
 * A [MessageFilter] accepting every agent.
 */
data object AcceptAll : MessageFilter<Any> {
    override fun accept(node: Node<out Any>, agent: AgentID, body: Any): Boolean = true
}

/**
 * A [MessageFilter] accepting only the [receiver] agent.
 */
data class SendTo(val receiver: AgentID) : MessageFilter<Any> {
    override fun accept(node: Node<out Any>, agent: AgentID, body: Any): Boolean = agent == receiver
}

/**
 * A [MessageFilter] accepting every agent except the [sender].
 */
data class BroadcastFrom(val sender: AgentID) : MessageFilter<Any> {
    override fun accept(node: Node<out Any>, agent: AgentID, body: Any): Boolean = agent != sender
}
