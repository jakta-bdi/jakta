package it.unibo.jakta.events


interface EventGenerator<out X : Event> {
    fun poll(): X?
}
