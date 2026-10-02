package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent

/**
 * A skill to read and write named properties of an agent that its environment can observe,
 * e.g. to be exported or displayed.
 */
interface PropertySkill {
    /**
     * The value of the property [name] of the agent, or null if it has none.
     */
    fun Agent.property(name: String): Any?

    /**
     * Sets the property [name] of the agent to [value], or removes it if [value] is null.
     */
    fun Agent.setProperty(name: String, value: Any?)
}

/**
 * The value of the property [name] of the agent, given by the [PropertySkill] in scope.
 */
context(skill: PropertySkill)
fun Agent.property(name: String): Any? = with(skill) { property(name) }

/**
 * Sets the property [name] of the agent to [value] with the [PropertySkill] in scope.
 */
context(skill: PropertySkill)
fun Agent.setProperty(name: String, value: Any?) = with(skill) { setProperty(name, value) }
