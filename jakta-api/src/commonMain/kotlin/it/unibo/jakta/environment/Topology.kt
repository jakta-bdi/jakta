package it.unibo.jakta.environment

interface Topology<Position: Any, Displacement: Any> {

    val defaultPosition: Position //TODO needed? here or elsewhere?

    fun isValid(position: Position) : Boolean

    fun distance(from: Position, to: Position) : Double

    fun move(from: Position, displacement: Displacement) : Position

}
