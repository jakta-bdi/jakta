package it.unibo.jakta.event

import kotlinx.coroutines.channels.Channel

internal class EventChannel (
    private val channel: Channel<Event> = Channel(Channel.UNLIMITED),
) : EventReceiver, EventSource {
    override fun trySend(event: Event) {
        channel.trySend(event)
    }

    override suspend fun next(): Event =
        channel.receive()

    override suspend fun send(event: Event) {
        channel.send(event)
    }
}


