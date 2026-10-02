package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent
import kotlin.random.Random

/**
 * A skill to draw random values from the random generator of the environment of an agent,
 * e.g. the seeded one of a simulation, so that runs are reproducible.
 */
interface RandomSkill {
    /**
     * The random generator of the environment.
     */
    val Agent.random: Random
}

/**
 * The random generator of the environment, given by the [RandomSkill] in scope.
 */
context(skill: RandomSkill)
val Agent.random: Random
    get() = with(skill) { random }
