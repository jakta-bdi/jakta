package it.unibo.jakta.agent

import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventReceiver
import it.unibo.jakta.event.EventSource
import it.unibo.jakta.intention.IntentionPool
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType

interface AgentBody {
    val id: AgentID
}

interface EnvironmentAgent<Body: AgentBody>{
    val inbox : EventReceiver<Event>
    val body: Body
}

interface AgentState<Belief: Any, Goal: Any, Skills: Any> : GuardScope<Belief> {
    /**
     * The *beliefs* currently held by the agent.
     */
    override val beliefs: Collection<Belief>

    /**
     * The pool of intentions currently being pursued by the agent.
     */
    val intentionPool: IntentionPool

    /**
     * The plans available to handle belief-related events.
     */
    val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>

    /**
     * The plans available to handle goal-related events.
     */
    val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>

    /**
     * Mapping function which defines how to (optionally) convert a [Event.External.Perception] into a [Event.Internal].
     */
    val perceptionHandler: (Event.External.Perception) -> Event.Internal?

    /**
     * Mapping function which defines how to (optionally) convert a [Event.External.Message] into a [Event.Internal].
     */
    val messageHandler: (Event.External.Message) -> Event.Internal?
}

interface MutableAgentState<Belief: Any, Goal: Any, Skills: Any> : AgentState<Belief, Goal, Skills> {

    fun setPerceptionHandler(handler: (Event.External.Perception) -> Event.Internal?)

    fun setMessageHandler(handler: (Event.External.Message) -> Event.Internal?)

    fun addPlan(plan: Plan<Belief, Goal, Skills,*, *, *>)

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     * !! This method is deprecated as it is an internal method that should not be used directly.
     * @param[goal] The goal to be achieved.
     * @param[resultType] The type of result expected from the plan that will handle this goal.
     * @return The result of the plan that achieved the goal.
     */
    @Deprecated("Use achieve instead", ReplaceWith("achieve(goal)"), DeprecationLevel.ERROR)
    suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete or return a result.
     * @param[goal] The goal to be achieved.
     */
    fun alsoAchieve(goal: Goal)

    /** Terminates the execution of the agent. **/
    suspend fun terminate()

    /**
     * Add the belief to the agent's belief base (eventually generating events).
     * @param belief The belief to be added.
     */
    fun believe(belief: Belief)

    /**
     * Remove the belief from the agent's belief base (eventually generating events).
     * @param belief The belief to be removed.
     */
    fun forget(belief: Belief)

}

interface RunnableAgent<Belief: Any, Goal: Any, Skills: Any> {
    val agentEvents : EventSource<Event>
    val state: MutableAgentState<Belief, Goal, Skills>
}

//TODO This is the interface to implement for the baseImpl
interface NewAgent<Belief: Any, Goal: Any, Skills: Any, Body: AgentBody> :
    RunnableAgent<Belief, Goal, Skills>,
    EnvironmentAgent<Body> {
}

//TODO separates the lifecycle form the agent definition
interface AgentLifecycle<Belief: Any, Goal: Any, Skills: Any> {

    val runnableAgent: RunnableAgent<Belief, Goal, Skills,>

    suspend fun step()

    suspend fun stop()
}

//TODO the engine its what was previously the MAS
interface Engine {
    suspend fun runAgent(agent: RunnableAgent<*, *, *>)
}

//TODO either needs a reference to the Engine or notifies it some other way of agent creation
interface Environment<Body: AgentBody> {
    val agentBodies: Collection<Body>
    val eventBroker: EventReceiver<Event.External>

    fun createAgent(body: Body, state: AgentState<*, *, *>)
}


// Example for spatial environmentsv

interface SpatialBody<P: Any> : AgentBody {
    val position: P
}

interface SpatialEnvironment<P: Any> : Environment<SpatialBody<P>>

interface MovementSkill<P: Any> {
    suspend fun moveTo(position: P)
}
