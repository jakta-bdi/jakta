package io.github.anitvam.agents.bdi.beliefs

/** Result of an update method over a BeliefBase */
data class RetrieveResult(
    /** Beliefs that are added or removed from the updatedBeliefBase */
    val modifiedBeliefs: List<BeliefUpdate>,

    /** The updated BeliefBase */
    val updatedBeliefBase: BeliefBase,
)

data class BeliefUpdate(
    val belief: Belief,
    val updateType: UpdateType,
) {
    companion object {
        fun removal(belief: Belief) = BeliefUpdate(belief, UpdateType.REMOVAL)
        fun addition(belief: Belief) = BeliefUpdate(belief, UpdateType.ADDITION)
    }

    enum class UpdateType {
        ADDITION,
        REMOVAL,
    }
}



