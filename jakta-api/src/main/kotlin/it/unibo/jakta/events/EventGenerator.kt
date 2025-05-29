package it.unibo.jakta.events

import java.util.*

interface EventGenerator<out X : Event> {
    fun poll(): X?
}
