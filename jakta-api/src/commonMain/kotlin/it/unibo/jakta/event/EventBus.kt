package it.unibo.jakta.event

/**
 * Represents a bus for handling events of type [E].
 * This interface combines the functionalities of both [EventInbox] and [EventStream],
 */
interface EventBus<E> :
    EventInbox<E>,
    EventStream<E>
