package it.unibo.jakta.agents.bdi.distributed

import it.unibo.jakta.agents.bdi.messages.Message

interface MessageBroker {
    fun putInMessageBox(
        receiver: String,
        message: Message,
    ): Unit

    fun send(
        receiver: String,
        host: String,
        message: Message,
    ): Unit

    fun broadcast(message: Message): Unit

    /**
     * Removes the message returned from message queue.
     * @return a [Message].
     */
    fun pop(
        receiver: String,
        host: String,
    ): Message?

    /** Does not remove the message returned from message queue.
     * @return a [Message].
     */
    fun nextMessage(
        receiver: String,
        host: String,
    ): Message?
}
