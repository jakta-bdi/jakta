package it.unibo.jakta.agent

import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventSource

interface RunnableAgent<Belief: Any, Goal: Any, Skills: Any> {
    val agentEvents : EventSource
    val state: AgentMutableState<Belief, Goal, Skills>
}
