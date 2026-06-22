package it.unibo.jakta.event

/**
 * Represents an update to an agent's beliefs or goals.
 * This sealed interface defines the structure for updates that can be applied to an agent's state,
 * including additions and removals of beliefs or goals.
 *
 * @param T The type of the elements being updated, which can be either beliefs or goals.
 */
sealed interface AgentUpdate<T : Any> {

    val additions: Set<T>
    val removals: Set<T>

    data class Belief<B : Any>(override val additions: Set<B>, override val removals: Set<B> = emptySet()) : AgentUpdate<B>

    data class Goal<G : Any>(override val additions: Set<G>, override val removals: Set<G> = emptySet()) : AgentUpdate<G>
}
