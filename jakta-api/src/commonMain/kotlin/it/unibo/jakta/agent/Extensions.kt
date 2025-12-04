package it.unibo.jakta.agent

import kotlin.reflect.typeOf

/**
 * Public-facing extension function to achieve a goal with a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 * @return The result of the plan execution of type [PlanResult].
 */
@Suppress("DEPRECATION_ERROR")
suspend inline fun <B : Any, G : Any, reified PlanResult> AgentActions<B, G>.achieve(goal: G): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())

// TODO(Ambiguous :((((( as expected. I'd like to avoid to specify the return type of achieve when this is Unit)
// @Suppress("DEPRECATION_ERROR")
// suspend fun <B: Any, G: Any> AgentActions<B, G>.achieve(goal: G) : Unit =
//    _achieve(goal, typeOf<Unit>())
