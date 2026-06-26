package it.unibo.jakta.agent

import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Mutable state of an agent, allowing modifications to its beliefs, plans, and event handlers.
 */
interface MutableAgentState<Belief : Any, Goal : Any> :
    AgentState<Belief, Goal>,
    Agent {

    /**
     * The mutable pool of intentions that the agent is currently pursuing.
     */
    val mutableIntentionPool: MutableIntentionPool

    /**
     * Modifies the perception handler function that defines which external events are of interest of the agent.
     * @param handler the new function handler the agent will use starting from next iteration of its lifecycle.
     */
    fun setPerceptionHandler(
        handler: AgentState<Belief, Goal>.(AgentEvent.External.Perception) -> AgentUpdate<*>?,
    )

    /**
     * Modifies the message handler function that defines which Messages are interest of the agent.
     * @param handler the new function handler that the agent will use, starting from the next lifecycle iteration.
     */
    fun setMessageHandler(handler: AgentState<Belief, Goal>.(AgentEvent.External.Message) -> AgentUpdate<*>?)

    /**
     * Adds a new [Plan.Goal] that agent can use for its reasoning process.
     * @param plan the new [Plan.Goal].
     */
    fun addPlan(plan: Plan.Goal<Belief, Goal, *, *>)

    /**
     * Adds a new [Plan.Belief] that agent can use for its reasoning process.
     * @param plan the new [Plan.Belief].
     */
    fun addPlan(plan: Plan.Belief<Belief, Goal, *, *>)

    /**
     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
     * !! This method is deprecated as it is an internal method that should not be used directly.
     * @param[goal] The goal to be achieved.
     * @param[resultType] The type of result expected from the plan that will handle this goal.
     * @return The result of the plan that achieved the goal.
     */
    @InternalJaktaAPI
    suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult

    /**
     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete or return a result.
     * @param[goal] The goal to be achieved.
     */
    fun alsoAchieve(goal: Goal)

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

    /**
     * Logs a message to the agent's output.
     * @param[message] The message to be printed.
     */
    fun print(message: String)
}

/**
 * Public-facing extension function to achieve a goal with a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 * @return The result of the plan execution of type [PlanResult].
 */
@OptIn(InternalJaktaAPI::class)
suspend inline fun <Goal : Any, reified PlanResult> MutableAgentState<*, Goal>.achieve(goal: Goal): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())
