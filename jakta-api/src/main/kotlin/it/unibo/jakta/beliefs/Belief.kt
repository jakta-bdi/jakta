package it.unibo.jakta.beliefs

interface Belief<out B> {
    val content: B
}
