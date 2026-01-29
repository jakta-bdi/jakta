package it.unibo.jakta.agent.basImpl

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.EnvironmentAgent
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.agent.RunnableAgent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.baseImpl.UnlimitedChannelBus
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.EventStream

/**
 * Default implementation of an [it.unibo.jakta.agent.Agent].
 */
class BaseAgent<Belief : Any, Goal : Any, Skills: Any, Body: AgentBody>(
    agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
) : Agent, RunnableAgent<Belief, Goal, Skills>, EnvironmentAgent<Body> {

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
        {}, //TODO(!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!)
    )

    /**
     * Initialize the agent by triggering the achievement of the initial goals.
     */
    init {
        agentSpecification.initialGoals.forEach { state.alsoAchieve(it)}
    }
}
