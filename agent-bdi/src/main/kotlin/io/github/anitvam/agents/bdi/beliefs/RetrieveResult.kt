package io.github.anitvam.agents.bdi.beliefs

/** Result of an update method over a BeliefBase */
data class RetrieveResult(
    /** Beliefs that are added or removed from the updatedBeliefBase */
    val modifiedBeliefs: List<Belief>,

    /** The updated BeliefBase */
    val updatedBeliefBase: BeliefBase,
)