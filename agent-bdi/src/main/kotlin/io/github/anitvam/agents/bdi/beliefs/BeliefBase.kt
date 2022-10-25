package io.github.anitvam.agents.bdi.beliefs

import io.github.anitvam.agents.bdi.beliefs.impl.BeliefBaseImpl
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution

/** A BDI Agent's collection of [Belief] */
interface BeliefBase : Iterable<Belief> {
    /**
     * Adds a [Belief] to this [BeliefBase]
     * @param belief: the [Belief] to be added
     * @return a [RetrieveResult] with the new [BeliefBase] and the added [Belief]
     **/
    fun add(belief: Belief): RetrieveResult

    /**
     * Adds all the given beliefs into this [BeliefBase]
     * @param beliefs: beliefs to be added
     * @return a [RetrieveResult] with the new [BeliefBase] and the added [Belief]s
     **/
    fun addAll(beliefs: BeliefBase): RetrieveResult

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @param belief: the [Belief] to be removed
     * @return a [RetrieveResult] with the new [BeliefBase] and the removed [Belief]
     */
    fun remove(belief: Belief): RetrieveResult

    /**
     * Removes all the specified beliefs from this [BeliefBase]
     * @param beliefs: beliefs to be removed
     * @return a [RetrieveResult] with the new [BeliefBase] and the removed [Belief]s
     */
    fun removeAll(beliefs: BeliefBase): RetrieveResult

    fun solve(struct: Struct) : Solution

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
