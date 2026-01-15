package it.unibo.jakta.agent.basImpl

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.EventStream

/**
 * Default implementation of an [it.unibo.jakta.agent.Agent].
 */
class BaseAgent<Belief : Any, Goal : Any, Skills: Any, Body: AgentBody>(
    agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
) : Agent<Belief, Goal, Skills, Body> {

    private val eventBus: EventBus<Event> = EventBus()

    override val events: EventStream<Event>
        get() = eventBus

    override val internalInbox: EventInbox<Event.Internal>
        get() = eventBus

    override val externalInbox: EventInbox<Event.External>
        get() = eventBus

    override val body: Body = agentSpecification.body

    override val state: MutableAgentState<Belief, Goal, Skills> = BaseMutableAgentState(
        agentSpecification.initialState,
        internalInbox,
        {}, //TODO(!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!)
    )

    /**
     * Initialize the agent by triggering the achievement of the initial goals.
     */
    init {
        agentSpecification.initialGoals.forEach { state.alsoAchieve(it)}
    }
}
