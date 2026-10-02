package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent

/**
 * A skill to know and change the position of an agent.
 */
interface SpatialSkill {
    /**
     * The current position of the agent.
     */
    val Agent.position: Coordinates

    /**
     * Moves the agent to [position].
     */
    fun Agent.moveTo(position: Coordinates)
}

/**
 * The current position of the agent, given by the [SpatialSkill] in scope.
 */
context(skill: SpatialSkill)
val Agent.position: Coordinates
    get() = with(skill) { position }

/**
 * Moves the agent to [position] with the [SpatialSkill] in scope.
 */
context(skill: SpatialSkill)
fun Agent.moveTo(position: Coordinates) = with(skill) { moveTo(position) }

/**
 * Moves the agent by [offset].
 */
context(skill: SpatialSkill)
fun Agent.moveBy(offset: Coordinates) = moveTo(position + offset)

/**
 * Moves the agent towards [target], covering at most [maxDistance].
 * Call it repeatedly, e.g. with a `delay` in between, to move at a given speed.
 * @return true if the agent reached the [target].
 */
context(skill: SpatialSkill)
fun Agent.moveTowards(target: Coordinates, maxDistance: Double): Boolean {
    val distance = position.distanceTo(target)
    val reached = distance <= maxDistance
    moveTo(if (reached) target else position + (target - position) * (maxDistance / distance))
    return reached
}
