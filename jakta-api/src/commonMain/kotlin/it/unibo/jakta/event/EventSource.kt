package it.unibo.jakta.event


interface EventSource<E: Event> {
    suspend fun next() : E
}


