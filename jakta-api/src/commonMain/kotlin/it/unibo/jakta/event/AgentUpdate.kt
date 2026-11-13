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
     * The beliefs in [scope] are now [replacements]: those in scope and not among them are forgotten, and the new ones
     * believed, while beliefs that do not change raise no events. Typically, what a perception tells about some part of
     * the environment. Each of [replacements] must be in [scope], or applying the update fails.
     *
     * @property scope for each belief held, the belief to remove from it, or `null` if it is out of scope: the belief
     * itself, or, for beliefs with metadata, the belief with only the metadata in scope (see
     * [it.unibo.jakta.belief.BeliefRevision]). Use [where] when a belief is either wholly in scope or not at all.
     */
    data class Replace<B : Any>(val replacements: List<B>, val scope: (B) -> B?) : AgentUpdate<B> {
        /**
         * Factory methods for [Replace].
         */
        companion object {
            /**
             * The beliefs that satisfy [filter] are now [replacements].
             */
            fun <B : Any> where(replacements: List<B>, filter: (B) -> Boolean): Replace<B> =
                Replace(replacements) { belief -> belief.takeIf(filter) }
        }
    }

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
