package it.unibo.jakta.beliefs

import it.unibo.jakta.context.Addition
import it.unibo.jakta.context.ContextUpdate
import it.unibo.jakta.context.Removal

/** Result of an update method over a PrologBeliefBase */
data class RetrieveResult<B : Belief<B>>(
    /** Beliefs that are added or removed from the updatedBeliefBase */
    val modifiedBeliefs: List<BeliefUpdate<B>>,

    /** The updated PrologBeliefBase */
    val updatedBeliefBase: BeliefBase<B>,
)

data class BeliefUpdate<B : Belief<B>>(
    val belief: B,
    val updateType: ContextUpdate,
) {
    companion object {
        fun <B : Belief<B>> removal(belief: B) = BeliefUpdate(belief, Removal)
        fun <B : Belief<B>> addition(belief: B) = BeliefUpdate(belief, Addition)
    }
}
