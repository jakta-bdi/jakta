package it.unibo.jakta.event

/**
 * Represents an update to an agent's beliefs or goals.
 * This sealed interface defines the structure for updates that can be applied to an agent's state,
 * including additions and removals of beliefs or goals.
 *
 * @param T The type of the elements being updated, which can be either beliefs or goals.
 */
sealed interface AgentUpdate<T : Any> {

    val additions: List<T>
    val removals: List<T>

    data class Belief<B : Any>(override val additions: List<B>, override val removals: List<B>) : AgentUpdate<B>

    data class Goal<G : Any>(override val additions: List<G>, override val removals: List<G>) : AgentUpdate<G>
}
