package it.unibo.jakta.agent

import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventReceiver
import it.unibo.jakta.event.EventSource

interface RunnableAgent<Belief: Any, Goal: Any, Skills: Any> {
    /**
     * A runnable agent generates [Event]s.
     */
    val agentEvents : EventSource<Event>

    val state: AgentMutableState<Belief, Goal, Skills>
}
