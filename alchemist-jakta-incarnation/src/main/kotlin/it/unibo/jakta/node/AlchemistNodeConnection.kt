package it.unibo.jakta.node

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue

object AlchemistNodeConnection {

    private val subscribers: MutableSet<EventQueue<SystemEvent>> = mutableSetOf()

    fun subscribe(node: Node<Any?>): Subscription {
        val queue = UnlimitedChannelQueue<SystemEvent>().also {
            subscribers.add(it)
        }
        return object : Subscription {
            override val queue: EventQueue<SystemEvent> = queue
            override suspend fun close() {
                subscribers.remove(queue)
            }

            override var node: Node<Any?> = node

            override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> = this.also { this.node = node }
        }
    }

    fun send(event: SystemEvent) {
        subscribers.forEach { it.send(event) }
    }

    interface Subscription : NodeSubscription, NodeProperty<Any?>
}
