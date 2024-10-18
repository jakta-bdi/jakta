package it.unibo.jakta.resolution

interface Solution<X> {
    val isSuccess: Boolean
    val result: X
}
