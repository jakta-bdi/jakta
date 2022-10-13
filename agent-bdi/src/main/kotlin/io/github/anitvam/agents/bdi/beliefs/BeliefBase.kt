package io.github.anitvam.agents.bdi.beliefs

import io.github.anitvam.agents.bdi.beliefs.impl.BeliefBaseImpl
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.collections.RetrieveResult

/** A BDI Agent's collection of [Belief] */
interface BeliefBase {
    /**
     * Adds a [Belief] to this [BeliefBase]
     * @param belief: the [Belief] to be added
     * @param onAdditionPerformed: the callback that will be invoked when the addition is performed, it takes the
     * added belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     **/
    fun add(belief: Belief, onAdditionPerformed: (addedBelief: Belief) -> Unit = {}): BeliefBase

    /**
     * Adds all the given beliefs into this [BeliefBase]
     * @param beliefs: beliefs to be added
     * @param onAdditionPerformed: the callback that will be invoked when each addition is performed, it takes the
     * added belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     **/
    fun addAll(beliefs: BeliefBase, onAdditionPerformed: (addedBelief: Belief) -> Unit = {}): BeliefBase

    /** Retrieves the first unifying [Belief] from this [BeliefBase] as a [RetrieveResult]**/
    fun retrieve(belief: Belief): RetrieveResult<out ClauseMultiSet>

    /** Retrieves all the unifying [Belief] from this [BeliefBase] as a [RetrieveResult]**/
    fun retrieveAll(belief: Belief): RetrieveResult<out ClauseMultiSet>

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @param belief: the [Belief] to be removed
     * @param onRemovalPerformed: the callback that will be invoked when the removal is performed, it takes the
     * removed belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     */
    fun remove(belief: Belief, onRemovalPerformed: (removedBelief: Belief) -> Unit = {}): BeliefBase

    /**
     * Removes all the specified beliefs from this [BeliefBase]
     * @param beliefs: beliefs to be removed
     * @param onRemovalPerformed: the callback that will be invoked when each one removal is performed, it takes the
     * removed belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     */
    fun removeAll(beliefs: BeliefBase, onRemovalPerformed: (removedBelief: Belief) -> Unit = {}): BeliefBase

    /**
     * Executes an action for each element of this [BeliefBase]
     * @param action: the action to be executed
     */
    fun forEachBelief(action: (Belief) -> Unit)

    /**
     * Returns a [BeliefBase] containing only elements matching the given filter.
     * @param filter: the predicate
     * @return a new [BeliefBase] with the elements matching the filter
     */
    fun filter(filter: (Belief) -> Boolean): BeliefBase

    /** @return true whether the belief is contained inside the [BeliefBase], otherwise false */
    fun contains(belief: Belief) : Boolean

    companion object {
        /** @return an empty [BeliefBase] */
        fun empty(): BeliefBase = BeliefBaseImpl(ClauseMultiSet.empty())

        /**
         * Generates a [BeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [BeliefBase] will be composed of
         * @return the new [BeliefBase]
         */
        fun of(beliefs: Iterable<Belief>): BeliefBase = BeliefBaseImpl(ClauseMultiSet.of(beliefs))
    }
}
