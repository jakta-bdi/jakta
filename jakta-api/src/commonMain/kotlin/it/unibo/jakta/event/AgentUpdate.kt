package it.unibo.jakta.event

/**
 * A change an agent is asked to make to its beliefs or goals, e.g. by its perception or message handlers.
 * Unlike [AgentEvent]s, which tell what happened, an update is a request: belief updates go through the agent's
 * belief revision, which decides what actually changes and which events are raised.
 *
 * @param T The type of the elements being updated, which can be either beliefs or goals.
 */
sealed interface AgentUpdate<T : Any> {
    /**
     * Believe each of [beliefs].
     */
    data class Believe<B : Any>(val beliefs: List<B>) : AgentUpdate<B> {
        constructor(vararg beliefs: B) : this(beliefs.toList())
    }

    /**
     * Forget each of [beliefs].
     */
    data class Forget<B : Any>(val beliefs: List<B>) : AgentUpdate<B> {
        constructor(vararg beliefs: B) : this(beliefs.toList())
    }

    /**
     * The beliefs in [scope] are now [beliefs]: those in scope and not among [beliefs] are forgotten, and the new ones
     * believed, while beliefs that do not change raise no events. Typically, what a perception tells about some part of
     * the environment.
     */
    data class Replace<B : Any>(val scope: (B) -> Boolean, val beliefs: List<B>) : AgentUpdate<B>

    /**
     * Adopt each of [goals].
     */
    data class Adopt<G : Any>(val goals: List<G>) : AgentUpdate<G> {
        constructor(vararg goals: G) : this(goals.toList())
    }

    /**
     * Drop each of [goals].
     */
    data class Drop<G : Any>(val goals: List<G>) : AgentUpdate<G> {
        constructor(vararg goals: G) : this(goals.toList())
    }
}
