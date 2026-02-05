package it.unibo.jakta.event.baseImpl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.event.SystemEvent

data class AgentAdditionEvent<Belief: Any, Goal: Any, Skills: Any>(
    override val executableAgent: ExecutableAgent<Belief, Goal, Skills>
) : SystemEvent.AgentAddition<Belief, Goal, Skills>

data class AgentRemovalEvent(
    override val id: AgentID
) : SystemEvent.AgentRemoval

object ShutDownMASEvent : SystemEvent.ShutDownMAS
