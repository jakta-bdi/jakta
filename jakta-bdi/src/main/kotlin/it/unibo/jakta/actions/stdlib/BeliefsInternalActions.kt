package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.actions.AbstractInternalAction
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature


abstract class AbstractBeliefInternalAction(
    var belief: ASBelief,
    name: String,
): AbstractInternalAction(Signature(name, 1)) {
    override fun applySubstitution(substitution: Substitution) {
        belief = belief.applySubstitution(substitution)
    }
}

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
class AddBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "addBelief") {
    override suspend fun action(request: InternalRequest) {
        addActionEffect(BeliefChange.BeliefAddition(belief))
    }
}

/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
class RemoveBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "addBelief")  {
    override suspend fun action(request: InternalRequest) {
        addActionEffect(BeliefChange.BeliefRemoval(belief))
    }
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
class UpdateBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "removeBelief")  {
    override suspend fun action(request: InternalRequest) {
        TODO("Missing implementation for update in beliefcontext")
    }
}

