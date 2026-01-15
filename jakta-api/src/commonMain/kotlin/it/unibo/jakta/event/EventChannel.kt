package it.unibo.jakta.event

import kotlinx.coroutines.channels.Channel

internal class EventChannel<E: Event>: EventReceiver<E>, EventSource<E> {

    private val channel: Channel<E> = Channel(Channel.UNLIMITED)

    override fun trySend(event: E) {
        channel.trySend(event)
    }

    override suspend fun next(): E =
        channel.receive()
}


