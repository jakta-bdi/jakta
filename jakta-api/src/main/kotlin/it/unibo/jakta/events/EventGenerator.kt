package it.unibo.jakta.events

import kotlinx.coroutines.flow.Flow

interface EventGenerator<X: Event> {
    val events: Flow<X>
}