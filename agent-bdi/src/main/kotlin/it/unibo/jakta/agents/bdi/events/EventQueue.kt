package it.unibo.jakta.agents.bdi.events

/**
 * The collection of [Event] that the BDI Agent reacts on.
 *
 * As in Jason, Events are modeled with a FIFO queue. Users can provide an agent-specific event selection function,
 * that handle how an event is chosen from the queue.
 */
typealias EventQueue = List<Event>
