package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.tuprolog.core.Substitution

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
data class AddBelief(
    val belief: ASBelief,
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution): AddBelief =
        AddBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        listOf(BeliefChange.BeliefAddition(belief))
}

/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
data class RemoveBelief(
    val belief: ASBelief,
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        RemoveBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        listOf(BeliefChange.BeliefRemoval(belief))
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(
    val belief: ASBelief,
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        UpdateBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        listOf(BeliefChange.BeliefUpdate(belief))
}
