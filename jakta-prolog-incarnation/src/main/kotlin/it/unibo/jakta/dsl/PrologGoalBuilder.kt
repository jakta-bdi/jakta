package it.unibo.jakta.dsl

import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.belief.specialFunctors
import it.unibo.jakta.goal.PrologGoal
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import kotlin.reflect.typeOf

context(scope: JaktaLogicProgrammingScope)
fun goal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal = Fact.of(
    scope.block().also {
        require(it.functor !in specialFunctors) { "Goal must be a predicate" }
    },
).also {
    require(it.isGround) { "Goal must be ground" }
}

context(scope: JaktaLogicProgrammingScope)
fun goalQuery(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal = Fact.of(scope.block())

/**
 * Public-facing extension function to achieve a goal with a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 * @return The result of the plan execution of type [PlanResult].
 */
@OptIn(InternalJaktaAPI::class)
@JvmName("achieveTyped")
suspend inline fun <reified PlanResult> MutableAgentState<*, PrologGoal, *>.achieve(goal: PrologGoal): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())

/**
 * Public-facing extension function to achieve a goal with a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 * @return The result of the plan execution of type [PlanResult].
 */
@OptIn(InternalJaktaAPI::class)
@JvmName("achieveUnit")
suspend inline fun MutableAgentState<*, PrologGoal, *>.achieve(goal: PrologGoal): Unit =
    internalAchieve(goal, typeOf<Unit>())
