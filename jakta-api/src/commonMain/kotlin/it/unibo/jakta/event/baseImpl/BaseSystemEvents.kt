package it.unibo.jakta.event.baseImpl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.RunnableAgent
import it.unibo.jakta.event.SystemEvent

data class AgentAdditionEvent<Belief: Any, Goal: Any, Skills: Any>(
    override val runnableAgent: RunnableAgent<Belief, Goal, Skills>
) : SystemEvent.AgentAddition<Belief, Goal, Skills>

data class AgentRemovalEvent(
    override val id: AgentID
) : SystemEvent.AgentRemoval

object ShutDownMASEvent : SystemEvent.ShutDownMAS
