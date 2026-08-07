package it.unibo.jakta.node

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue

/**
 * Shared queue of events that are managed by [JaktaForAlchemistNode]s executing in the simulation.
 * Each [JaktaForAlchemistNode] holds its subscription to the [NodeNetwork].
 */
object NodeNetwork {

    private val subscribers: MutableSet<EventQueue<SystemEvent>> = mutableSetOf()

    /**
     * @return a new [NodeSubscription] which registers itself for receiving the shared events.
     */
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

    /**
     * Sends a [SystemEvent] to all the [JaktaForAlchemistNode] registered to this [NodeNetwork].
     * @param event the [SystemEvent] that is being shared.
     */
    fun send(event: SystemEvent) {
        subscribers.forEach { it.send(event) }
    }
}
