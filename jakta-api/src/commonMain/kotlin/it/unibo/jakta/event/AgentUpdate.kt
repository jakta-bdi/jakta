package it.unibo.jakta.event

/**
 * Represents an update to an agent's beliefs or goals.
 * This sealed interface defines the structure for updates that can be applied to an agent's state,
 * including additions and removals of beliefs or goals.
 *
 * @param T The type of the elements being updated, which can be either beliefs or goals.
 */
sealed interface AgentUpdate<T : Any> {
    /**
     * The set of elements to be added to the agent's state.
     */
    val additions: Set<T>

    /**
     * The set of elements to be removed from the agent's state.
     */
    val removals: Set<T>

    /**
     * Represents an update to an agent's beliefs, coming from an external source.
     * The agent will try to believe all the beliefs in [additions] and forget all the beliefs in [removals].
     */
    data class Belief<B : Any>(override val additions: Set<B>, override val removals: Set<B> = emptySet()) :
        AgentUpdate<B>

    /**
     * Represents an update to an agent's goals coming from an external source.
     * The agent will achieve all the goals in [additions] and remove all the goals in [removals].
     */
    data class Goal<G : Any>(override val additions: Set<G>, override val removals: Set<G> = emptySet()) :
        AgentUpdate<G>
}
