package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

sealed class AbstractBeliefAction : AbstractAction() {
    override fun postExec(intention: ASIntention) {
        intention.pop()
    }
}

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
data class AddBelief(
    val belief: ASBelief,
) : AbstractBeliefAction() {
    override fun applySubstitution(substitution: Substitution): AddBelief =
        AddBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ASActionContext): List<SideEffect> =
        listOf(BeliefChange.BeliefAddition(belief))
}

/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
data class RemoveBelief(
   val belief: ASBelief,
) : AbstractBeliefAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        RemoveBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ASActionContext): List<SideEffect> =
        listOf(BeliefChange.BeliefRemoval(belief))
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(
    val belief: ASBelief,
) : AbstractBeliefAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        UpdateBelief(belief.applySubstitution(substitution))

    override fun invoke(context: ASActionContext): List<SideEffect> =
        listOf(BeliefChange.BeliefUpdate(belief))
}
