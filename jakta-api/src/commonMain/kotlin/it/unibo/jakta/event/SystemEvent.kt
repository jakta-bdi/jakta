package it.unibo.jakta.event

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent


sealed interface SystemEvent {

    interface AgentAddition<Belief: Any, Goal: Any, Skills: Any> : SystemEvent {
        val executableAgent: ExecutableAgent<Belief, Goal, Skills>
    }

    interface AgentRemoval : SystemEvent {
        val id: AgentID
    }

    interface ShutDownMAS : SystemEvent
}
