package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent
import kotlin.time.Duration

/**
 * A skill to read the time of the environment of an agent, e.g. the simulated time in a simulator.
 */
interface ClockSkill {
    /**
     * The time elapsed since the environment started.
     */
    val Agent.time: Duration
}

/**
 * The time elapsed since the environment started, given by the [ClockSkill] in scope.
 */
context(skill: ClockSkill)
val Agent.time: Duration
    get() = with(skill) { time }
