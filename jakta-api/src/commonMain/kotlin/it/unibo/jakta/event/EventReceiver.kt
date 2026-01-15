package it.unibo.jakta.event

import kotlinx.coroutines.channels.Channel

interface EventReceiver {
    fun trySend(event: Event)

    suspend fun send(event: Event)
}


