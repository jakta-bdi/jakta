package it.unibo.jakta.event

import kotlinx.coroutines.channels.Channel

interface EventReceiver<in E: Event> {
    fun trySend(event: E)

}


