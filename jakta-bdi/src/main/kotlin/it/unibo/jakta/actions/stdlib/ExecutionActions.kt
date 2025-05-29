package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

class Print(vararg terms: Term) : AbstractAction.WithoutSideEffects() {
    private val termsList: List<Term> = terms.toList()

    constructor(list: List<Term>) : this(*list.toTypedArray())

    override fun applySubstitution(substitution: Substitution) = Print(termsList.map { it.apply(substitution) })

    override fun execute(context: ASActionContext) {
        val payload =
            termsList.joinToString(" ") {
                when {
                    it.isAtom -> it.castToAtom().value
                    else -> it.toString()
                }
            }
        println("[${context.agentContext.agentName}] $payload")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Print) return false
        return termsList.containsAll(other.termsList)
    }

    override fun hashCode(): Int = termsList.hashCode()
}

object Fail : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ASActionContext) {
        result = Substitution.failed()
    }
}

object Stop : AbstractAction() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun invoke(context: ASActionContext) = listOf(Stop)
}

object Pause : AbstractAction() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun invoke(context: ASActionContext) = listOf(Pause)
}

class Sleep(private val timeAmount: Term) : AbstractAction() {
    override fun applySubstitution(substitution: Substitution) = Sleep(timeAmount.apply(substitution))

    override fun invoke(context: ASActionContext) = listOf(Sleep(timeAmount.castToInteger().value.toLong()))
}
