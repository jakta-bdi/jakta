package it.unibo.jakta.events

import java.util.*

interface EventGenerator<X: Event> {
    val events: Queue<X>
}