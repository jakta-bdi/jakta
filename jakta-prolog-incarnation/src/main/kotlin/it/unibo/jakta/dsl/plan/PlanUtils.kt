package it.unibo.jakta.dsl.plan

import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.PlanScope
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution
import kotlin.reflect.typeOf

/**
 * Checks if the current beliefs satisfy the given guard condition.
 * @param block A lambda function that defines the guard condition to be checked against the current beliefs.
 * @param scope The [JaktaLogicProgrammingScope] in which the guard condition is defined.
 * @receiver The [GuardScope] containing the current beliefs and context.
 * @return The matched [Substitution] if the guard condition is satisfied, or null if it is not satisfied.
 */
@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun GuardScope<PrologBelief, MutableSubstitutionPlanContext>.satisfies(
    block: JaktaLogicProgrammingScope.() -> Struct,
): MutableSubstitutionPlanContext? {
    val guard = scope.block()
    val substitutedGuard = guard.apply(this.context.substitution).castToStruct()
    return when (val solution = this.beliefs.unifiesWith(substitutedGuard)) {
        is Solution.Yes -> {
            this.context += solution.substitution
            this.context
        }

        else -> null
    }
}

/**
 * Tests a prolog query on the agent's current beliefs and if it succeeds the result of the substitution is merged
 * with the current plan context.
 * @param block A lambda function that defines the beliefQuery query to be created.
 * @return The created [Fact] as a query if it is a valid predicate.
 */
// TODO test this
context(logicScope: JaktaLogicProgrammingScope)
fun PlanScope<PrologBelief, PrologGoal, MutableSubstitutionPlanContext>.testQuery(
    block: JaktaLogicProgrammingScope.() -> Struct,
) {
    val query = logicScope.block().apply(this.context.substitution).castToStruct()
    when (val solution = this.agent.beliefs.unifiesWith(query)) {
        is Solution.Yes -> {
            this.context += solution.substitution
        }

        else -> error("The query $query is not satisfied")
    }
}

/**
 * Tests a prolog query on the agent's current beliefs and if it succeeds the result of the substitution is merged
 * with the current plan context.
 * @param block A lambda function that defines the beliefQuery query to be created.
 * @return The created [Fact] as a query if it is a valid predicate.
 */
// TODO test this
context(logicScope: JaktaLogicProgrammingScope)
fun MutableAgentState<PrologBelief, PrologGoal>.forgetAllMatching(
    beliefQuery: Struct,
) {

    TODO()
}

/**
 * Public-facing extension function to achieve a goal with a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 * @return The result of the plan execution of type [PlanResult].
 */
@OptIn(InternalJaktaAPI::class)
@JvmName("achieveTyped")
suspend inline fun <reified PlanResult> MutableAgentState<*, PrologGoal>.achieve(goal: PrologGoal): PlanResult =
    internalAchieve(goal, typeOf<PlanResult>())

/**
 * Public-facing extension function to achieve a goal without a specific return type, using reified type parameters.
 * @param goal The goal to be achieved.
 */
@OptIn(InternalJaktaAPI::class)
@JvmName("achieveUnit")
suspend inline fun MutableAgentState<*, PrologGoal>.achieve(goal: PrologGoal): Unit =
    internalAchieve(goal, typeOf<Unit>())
