package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractBeliefAction(
    var belief: ASBelief,
    name: String,
) : AbstractAction(Signature(name, 1)) {
    override fun applySubstitution(substitution: Substitution) {
        belief = belief.applySubstitution(substitution)
    }

    override fun postExec(intention: ASIntention) {
        intention.pop()
    }
}

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
class AddBelief(
    belief: ASBelief,
) : AbstractBeliefAction(belief, "addBelief") {
    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        listOf(BeliefChange.BeliefAddition(belief))
}

/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
class RemoveBelief(
    belief: ASBelief,
) : AbstractBeliefAction(belief, "addBelief") {
    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        listOf(BeliefChange.BeliefRemoval(belief))
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
class UpdateBelief(
    belief: ASBelief,
) : AbstractBeliefAction(belief, "removeBelief") {
    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        TODO("Missing implementation for update in beliefcontext")
}
