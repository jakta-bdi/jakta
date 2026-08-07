package it.unibo.jakta.node

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue

object NodeNetwork {

    private val subscribers: MutableSet<EventQueue<SystemEvent>> = mutableSetOf()

    fun subscribe(): NodeSubscription {
        val queue = UnlimitedChannelQueue<SystemEvent>().also {
            subscribers.add(it)
        }
        return object : NodeSubscription {
            override val queue: EventQueue<SystemEvent> = queue
            override suspend fun close() {
                subscribers.remove(queue)
            }
        }
    }

    fun send(event: SystemEvent) {
        subscribers.forEach { it.send(event) }
    }
}
