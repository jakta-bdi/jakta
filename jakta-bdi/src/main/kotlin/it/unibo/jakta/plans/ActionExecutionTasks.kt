package it.unibo.jakta.plans

import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.actions.AbstractInternalAction
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.Intention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
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
        addActionResult(
            BeliefChange.BeliefAddition(belief)
        )
    }
}


/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
class RemoveBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "addBelief")  {
    override suspend fun action(request: InternalRequest) {
        request.agent.context.removeBelief(belief)
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

