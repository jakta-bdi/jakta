package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
data class AddBelief(val belief: ASBelief) : AbstractAction() {
    override fun applySubstitution(substitution: Substitution): AddBelief =
        AddBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ActionInvocationContext<ASBelief, Struct, Solution>): List<SideEffect> =
        listOf(BeliefChange.Addition(belief))
}

/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
data class RemoveBelief(val belief: ASBelief) : AbstractAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        RemoveBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ActionInvocationContext<ASBelief, Struct, Solution>): List<SideEffect> =
        listOf(BeliefChange.Removal(belief))
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(val belief: ASBelief) : AbstractAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        UpdateBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ActionInvocationContext<ASBelief, Struct, Solution>): List<SideEffect> =
        listOf(BeliefChange.Update(belief))
}
