package it.unibo.jakta.dsl.plan

import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.PlanScope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution
import kotlin.reflect.typeOf

@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun GuardScope<PrologBelief, Substitution>.condition(guard: JaktaLogicProgrammingScope.() -> Struct): Substitution? {
    val guard = scope.guard()
    val substitutedGuard = guard.apply(this.context).castToStruct()
    return when (val solution = this.beliefs.unifiesWith(substitutedGuard)) {
        is Solution.Yes -> solution.substitution + this.context
        else -> null
    }
}

fun <Skills : Any> PlanLibraryBuilder<PrologBelief, PrologGoal, Skills>.prologPlan(
    block: context(JaktaLogicProgrammingScope) PlanLibraryBuilder<PrologBelief, PrologGoal, Skills>.() -> Unit,
): Unit = context(JaktaLogicProgrammingScope()) {
    this.block()
}

context(scope: JaktaLogicProgrammingScope)
fun <Skills : Any, PlanResult> withPrologContext(
    block: suspend context(JaktaLogicProgrammingScope)
    PlanScope<PrologBelief, PrologGoal, Skills, Substitution>.() -> PlanResult,
): suspend PlanScope<PrologBelief, PrologGoal, Skills, Substitution>.() -> PlanResult = {
    val planScope = this
    with(scope) {
        block()
    }
}

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
