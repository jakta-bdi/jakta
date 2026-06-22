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


context(substitution: Substitution)
val Var.value : Term
    get() = substitution[this]
        ?: error { "Variable $this is not ground in the substitution $substitution" }

context(substitution: Substitution)
fun <T : Any> Var.toKotlin(): T =
    this.value.accept(TermObjectifier.default) as? T
        ?: error { "Term $this cannot be cast to the expected type" }

context(substitution: Substitution)
fun MutableAgentState<PrologBelief, PrologGoal, *>.print(vararg parts: Any?) {
    val text = buildString {
        for (part in parts) {
            append(
                when (part) {
                    is Var -> part.value
                    else -> part
                }
            )
        }
    }
    print(text)
}
