package it.unibo.jakta

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.serialize.TermObjectifier

/**
 * Extension property to retrieve the value of a variable from a substitution.
 * @receiver The variable whose value is to be retrieved.
 * @return The value of the variable as a [Term].
 * @throws IllegalArgumentException if the variable is not ground in the substitution.
 */
context(substitution: Substitution)
val Var.value: Term
    get() = substitution[this]
        ?: error { "Variable $this is not ground in the substitution $substitution" }

/**
 * Extension function to convert a variable to a Kotlin type using the provided substitution.
 * @receiver The variable to be converted.
 * @return The value of the variable as the specified Kotlin type [T].
 * @throws IllegalArgumentException if the variable cannot be cast to the expected type.
 */
@Suppress("UNCHECKED_CAST")
context(substitution: Substitution)
fun <T : Any> Var.toKotlin(): T = this.value.accept(TermObjectifier.default) as? T
    ?: error { "Term $this cannot be cast to the expected type" }

/**
 * Extension function to print 2p-kt logic variables substituted with their values from the current substitution.
 * @receiver The mutable agent state containing the current substitution.
 * @param parts The parts to be printed, which can include variables and other objects.
 */
context(substitution: Substitution)
fun MutableAgentState<PrologBelief, PrologGoal>.print(vararg parts: Any?) {
    val text = buildString {
        for (part in parts) {
            append(
                when (part) {
                    is Var -> part.value
                    else -> part
                },
            )
        }
    }
    print(text)
}
