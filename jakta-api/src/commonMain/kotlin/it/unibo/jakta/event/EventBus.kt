package it.unibo.jakta.event

import kotlinx.coroutines.channels.Channel

internal class EventBus<E: Event>: EventInbox<E>, EventStream<E> {

    private val channel: Channel<E> = Channel(Channel.UNLIMITED)

    override fun send(event: E) {
        channel.trySend(event)
    }

    override suspend fun next(): E =
        channel.receive()
}


