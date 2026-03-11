package it.unibo.jakta.event

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent

data class AgentAdditionEvent<Belief : Any, Goal : Any, Skills : Any>(
    override val executableAgent: ExecutableAgent<Belief, Goal, Skills>,
) : SystemEvent.AgentAddition<Belief, Goal, Skills>

data class AgentRemovalEvent(override val id: AgentID) : SystemEvent.AgentRemoval

object ShutDownNodeEvent : SystemEvent.ShutDownNode
