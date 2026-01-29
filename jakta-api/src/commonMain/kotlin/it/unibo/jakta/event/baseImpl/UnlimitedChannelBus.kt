package it.unibo.jakta.event.baseImpl

import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.EventStream
import kotlinx.coroutines.channels.Channel

class UnlimitedChannelBus<E>: EventBus<E> {

    private val channel: Channel<E> = Channel(Channel.Factory.UNLIMITED)

    override fun send(event: E) {
        channel.trySend(event)
    }

    override suspend fun next(): E =
        channel.receive()
}
