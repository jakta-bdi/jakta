package it.unibo.jakta.agent

import kotlin.reflect.KType
import kotlin.reflect.typeOf

// TODO it would be nice to have this as an "internal interface" offering simple building-blocks methods,
//  and implement more complex actions as extension functions on top of it.
//  but maybe it won't give much benefits.
//
///**
// * This interface offers internal actions that can be used in plans.
// */
//interface AgentActions<Belief : Any, Goal : Any> {
//    /**
//     * Logs a message to the agent's output.
//     * @param[message] The message to be printed.
//     */
//    fun print(message: String)
//
//    /**
//     * Adds an event to the agent's queue to achieve a goal and suspends until the goal is achieved.
//     * !! This method is deprecated as it is an internal method that should not be used directly.
//     * @param[goal] The goal to be achieved.
//     * @param[resultType] The type of result expected from the plan that will handle this goal.
//     * @return The result of the plan that achieved the goal.
//     */
//    @Deprecated("Use achieve instead", ReplaceWith("achieve(goal)"), DeprecationLevel.ERROR)
//    suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult

//    /**
//     * Adds an event to the agent's queue to achieve a goal and don't wait for it to complete or return a result.
//     * @param[goal] The goal to be achieved.
//     */
//    fun alsoAchieve(goal: Goal)
//
//    /** Terminates the execution of the agent. **/
//    suspend fun terminate()
//
//    /**
//     * Add the belief to the agent's belief base (eventually generating events).
//     * @param belief The belief to be added.
//     */
//    suspend fun believe(belief: Belief)
//
//    /**
//     * Remove the belief from the agent's belief base (eventually generating events).
//     * @param belief The belief to be removed.
//     */
//    suspend fun forget(belief: Belief)
//}
