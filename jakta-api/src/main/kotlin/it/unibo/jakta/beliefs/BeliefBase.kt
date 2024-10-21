package it.unibo.jakta.beliefs

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<Query : Any, Belief, SelfType: BeliefBase<Query, Belief, SelfType>> : Collection<Belief> {

    /**
     * List of [BeliefUpdate] that populated the current [BeliefBase].
     **/
    val delta: List<Update<Belief>>

    /**
     * Resets the [BeliefBase] operations.
     * @return a [BeliefBase] where the delta list is empty.
     */
    fun resetDelta(): SelfType

    /**
     * Adds a [Belief] to this [BeliefBase]
     * @param belief: the [Belief] to be added
     * @return the new [BeliefBase] and the added [Belief]
     **/
    operator fun plus(belief: Belief): SelfType

    /**
     * Adds all the given beliefs into this [BeliefBase]
     * @param beliefs: beliefs to be added
     * @return the new [BeliefBase] and the added [Belief]s
     **/
    operator fun plus(beliefs: BeliefBase<Query, Belief>): SelfType

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @param belief: the [Belief] to be removed
     * @return the new [BeliefBase] and the removed [Belief]
     */
    operator fun minus(belief: Belief): SelfType

    /**
     * Removes all the specified beliefs from this [BeliefBase]
     * @param beliefs: beliefs to be removed
     * @return the new [BeliefBase] and the removed [Belief]s
     */
    operator fun minus(beliefs: BeliefBase<Query, Belief>): BeliefBase<Query, Belief>

    /**
     * Performs unification between [B] and values in this [BeliefBase]
     */
    fun solve(query: Query): SelfType

    // TODO: this may be a container of multiple updates that happen atomically
    sealed interface Update<Belief> {
        val diff: Belief

        data class Addition<Belief>(override val diff: Belief) : Update<Belief>
        data class Removal<Belief>(override val diff: Belief) : Update<Belief>
//        data object NoOp : Update<Nothing> {
//            override val diff: Nothing = error("ASSDASDADAS")
//        }
    }

    interface
}
