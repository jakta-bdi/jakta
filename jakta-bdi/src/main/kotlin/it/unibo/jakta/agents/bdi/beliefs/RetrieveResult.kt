package it.unibo.jakta.agents.bdi.beliefs

import it.unibo.jakta.agents.bdi.context.ContextUpdate
import it.unibo.jakta.agents.bdi.context.ContextUpdate.ADDITION
import it.unibo.jakta.agents.bdi.context.ContextUpdate.REMOVAL

/** Result of an update method over a BeliefBase */
data class RetrieveResult(
    /** Beliefs that are added or removed from the updatedBeliefBase */
    val modifiedBeliefs: List<BeliefUpdate>,
    /** The updated BeliefBase */
    val updatedBeliefBase: BeliefBase,
)

data class BeliefUpdate(
    val belief: Belief,
    val updateType: ContextUpdate,
) {
    companion object {
        fun removal(belief: Belief) = BeliefUpdate(belief, REMOVAL)

        fun addition(belief: Belief) = BeliefUpdate(belief, ADDITION)
    }
}
