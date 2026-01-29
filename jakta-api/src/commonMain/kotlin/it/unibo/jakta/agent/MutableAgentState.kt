package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Mutable state of an agent, allowing modifications to its beliefs, plans, and event handlers.
 */
interface MutableAgentState<Belief: Any, Goal: Any, Skills: Any> : AgentState<Belief, Goal, Skills> {

    /**
     * The mutable pool of intentions that the agent is currently pursuing.
     */
    val mutableIntentionPool: MutableIntentionPool

    /**
     * Modifies the perception handler function that defines which external events are of interest of the agent.
     * @param handler the new function handler the agent will use starting from next iteration of its lifecycle.
     */
    fun setPerceptionHandler(handler: (AgentEvent.External.Perception) -> AgentEvent.Internal?)

    /**
     * Modifies the message handler function that defines which Messages are interest of the agent.
     * @param handler the new function handler that the agent will use, starting from the next lifecycle iteration.
     */
    fun setMessageHandler(handler: (AgentEvent.External.Message) -> AgentEvent.Internal?)

    /**
     * Adds a new [Plan.Goal] that agent can use for its reasoning process.
     * @param plan the new [Plan.Goal].
     */
    fun addPlan(plan: Plan.Goal<Belief, Goal, Skills,*, *>)

    /**
     * Adds a new [Plan.Belief] that agent can use for its reasoning process.
     * @param plan the new [Plan.Belief].
     */
    fun addPlan(plan: Plan.Belief<Belief, Goal, Skills,*, *>)

    // TODO How to remove plans??

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
    suspend fun believe(belief: Belief)

    /**
     * Remove the belief from the agent's belief base (eventually generating events).
     * @param belief The belief to be removed.
     */
    suspend fun forget(belief: Belief)

    //TODO more advanced belief management? update belief? filter? etc.
    // do we provide these as utility extension functions?

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
@Suppress("DEPRECATION_ERROR")
suspend inline fun <B : Any, G : Any, S: Any, reified PlanResult> MutableAgentState<B, G, S>.achieve(goal: G): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())
