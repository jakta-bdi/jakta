package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.UnlimitedChannelQueue

/**
 * Default implementation of an [it.unibo.jakta.agent.Agent].
 */
class BaseAgent<Belief : Any, Goal : Any, Body : Any>(agentSpecification: AgentSpecification<Belief, Goal, Body>) :
    Agent,
    ExecutableAgent<Belief, Goal>,
    RuntimeAgent<Body> {

    override val id: AgentID = agentSpecification.id

    private val eventQueue: EventQueue<AgentEvent> = UnlimitedChannelQueue()

    override val events: EventStream<AgentEvent>
        get() = eventQueue

    override val internalInbox: EventInbox<AgentEvent.Internal>
        get() = eventQueue

    override val externalInbox: EventInbox<AgentEvent.External>
        get() = eventQueue

    override val body: Body = agentSpecification.body

    private val mutableState = BaseMutableAgentState(agentSpecification.initialState, internalInbox, id)

    override val state: MutableAgentState<Belief, Goal>
        get() = mutableState

    override fun resumeWaiting(event: AgentEvent) = mutableState.resumeWaiting(event)

    init {
        agentSpecification.initialGoals.forEach { state.alsoAchieve(it) }
    }
}
