package it.unibo.jakta.event

import kotlinx.coroutines.channels.Channel

/**
 * An implementation of [EventQueue] that uses an unbounded channel to handle events of type [E].
 */
class UnlimitedChannelQueue<E> : EventQueue<E> {

    private val channel: Channel<E> = Channel(Channel.Factory.UNLIMITED)

    override fun send(event: E) {
        channel.trySend(event)
    }

    override suspend fun next(): E = channel.receive()

    override fun tryNext(): E? = channel.tryReceive().getOrNull()
}
