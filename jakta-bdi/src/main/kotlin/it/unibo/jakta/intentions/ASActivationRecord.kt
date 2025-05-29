package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

data class ASActivationRecord(
    override val origin: Plan<ASBelief, Struct, Solution>,
    override val queue: Sequence<ASAction>,
) : ActivationRecord<ASBelief, Struct, Solution> {
    fun isLastActionToExecute(): Boolean = queue.count() == 1
    fun nextActionToExecute(): ASAction? = queue.firstOrNull()
    fun pop(): ASActivationRecord = ASActivationRecord(origin, when (nextActionToExecute() != null) {
        true -> queue - nextActionToExecute()!!
        false -> queue
    })
    fun applySubstitution(substitution: Substitution) = ASActivationRecord(
        origin,
        queue.map { it.applySubstitution(substitution) }
    )
}
