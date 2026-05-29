package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.UnlimitedChannelBus

/**
 * Default implementation of an [it.unibo.jakta.agent.Agent].
 */
class BaseAgent<Belief : Any, Goal : Any, Skills : Any, Body : Any>(
    agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
) : Agent,
    ExecutableAgent<Belief, Goal, Skills>,
    RuntimeAgent<Body> {

    override val id: AgentID = agentSpecification.id

    private val eventBus: EventBus<AgentEvent> = UnlimitedChannelBus()

    override val events: EventStream<AgentEvent>
        get() = eventBus

    override val internalInbox: EventInbox<AgentEvent.Internal>
        get() = eventBus

    override val externalInbox: EventInbox<AgentEvent.External>
        get() = eventBus

    override val body: Body = agentSpecification.body

    override val state: MutableAgentState<Belief, Goal, Skills> = BaseMutableAgentState(
        agentSpecification.initialState,
        internalInbox,
        id,
    )

    /**
     * Initialize the agent by triggering the achievement of the initial goals.
     */
    init {
        agentSpecification.initialGoals.forEach { state.alsoAchieve(it) }
    }
}
