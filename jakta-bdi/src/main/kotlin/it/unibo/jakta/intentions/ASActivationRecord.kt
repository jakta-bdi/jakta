package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

data class ASActivationRecord(
    override val origin: Plan<ASBelief, Struct, Solution>,
    override val queue: Sequence<Action<ASBelief, Struct, Solution>>,
) : ActivationRecord<ASBelief, Struct, Solution> {

    override fun pop(): ActivationRecord<ASBelief, Struct, Solution> = ASActivationRecord(origin, when (nextActionToExecute() != null) {
        true -> queue - nextActionToExecute()!!
        false -> queue
    })

    fun applySubstitution(substitution: Substitution) = ASActivationRecord(
        origin,
        queue.map { (it as? ASAction)?.applySubstitution(substitution) ?: error("Unsupported action type $it")}
    )
}
