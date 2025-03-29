package it.unibo.jakta.agents.bdi.beliefs

import it.unibo.jakta.agents.bdi.beliefs.impl.BeliefBaseImpl
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

    fun update(belief: Belief): RetrieveResult

    fun solve(struct: Struct): Solution

    fun solve(belief: Belief): Solution

    companion object {
        /** @return an empty [BeliefBase] */
        fun empty(): BeliefBase = BeliefBaseImpl()

        /**
         * Generates a [BeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [BeliefBase] will be composed of
         * @return the new [BeliefBase]
         */
        fun of(beliefs: Iterable<Belief>): BeliefBase {
            var bb = empty()
            beliefs.forEach { bb = bb.add(it).updatedBeliefBase }
            return bb
        }

        fun of(vararg beliefs: Belief): BeliefBase = of(beliefs.asList())
    }
}
