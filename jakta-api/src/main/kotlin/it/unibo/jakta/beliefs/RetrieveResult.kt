package it.unibo.jakta.beliefs

import it.unibo.jakta.context.Addition
import it.unibo.jakta.context.ContextUpdate
import it.unibo.jakta.context.Removal

/** Result of an update method over a PrologBeliefBase */
data class RetrieveResult<B : Belief<*>, C : BeliefBase<B, C>>(
    /** Beliefs that are added or removed from the updatedBeliefBase */
    val modifiedBeliefs: List<BeliefUpdate<B>>,

    /** The updated PrologBeliefBase */
    val updatedBeliefBase: BeliefBase<B, C>,
)

data class BeliefUpdate<B : Belief<*>>(
    val belief: B,
    val updateType: ContextUpdate,
) {
    companion object {
        fun <B : Belief<*>> removal(belief: B) = BeliefUpdate(belief, Removal)
        fun <B : Belief<*>> addition(belief: B) = BeliefUpdate(belief, Addition)
    }
}
