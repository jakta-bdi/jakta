package it.unibo.jakta.event

/**
 * Represents an inbox for receiving events of type [E].
 * This interface defines a contract for sending events to the inbox.
 *
 * @param E The type of events that this inbox can receive.
 */
interface EventInbox<in E> {
    /**
     * Sends an event of type [E] to the inbox.
     */
    fun send(event: E)
}
