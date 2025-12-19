package it.unibo.jakta.event

interface EventReceiver<E: Event> {

    suspend fun send(event: E)
}
