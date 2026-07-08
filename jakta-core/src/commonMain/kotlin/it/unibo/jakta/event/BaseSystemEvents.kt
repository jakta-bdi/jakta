package it.unibo.jakta.event

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent

/**
 * Base implementation of [SystemEvent.AgentAddition].
 */
data class AgentAdditionEvent<Belief : Any, Goal : Any>(override val executableAgent: ExecutableAgent<Belief, Goal>) :
    SystemEvent.AgentAddition<Belief, Goal>

/**
 * Base implementation of [SystemEvent.AgentRemoval].
 */
data class AgentRemovalEvent(override val id: AgentID) : SystemEvent.AgentRemoval

/**
 * Base implementation of [SystemEvent.ShutDownNode].
 */
object ShutDownNodeEvent : SystemEvent.ShutDownNode
