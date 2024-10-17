package it.unibo.jakta.beliefs

import it.unibo.jakta.resolution.Solution

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<B : Belief<B>> : Iterable<B> {
    /**
     * Adds a [Belief] to this [BeliefBase]
     * @param belief: the [Belief] to be added
     * @return a [RetrieveResult] with the new [BeliefBase] and the added [Belief]
     **/
    fun add(belief: B): RetrieveResult<B>

    /**
     * Adds all the given beliefs into this [BeliefBase]
     * @param beliefs: beliefs to be added
     * @return a [RetrieveResult] with the new [BeliefBase] and the added [Belief]s
     **/
    fun addAll(beliefs: BeliefBase<B>): RetrieveResult<B>

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @param belief: the [Belief] to be removed
     * @return a [RetrieveResult] with the new [BeliefBase] and the removed [Belief]
     */
    fun remove(belief: B): RetrieveResult<B>

    /**
     * Removes all the specified beliefs from this [BeliefBase]
     * @param beliefs: beliefs to be removed
     * @return a [RetrieveResult] with the new [BeliefBase] and the removed [Belief]s
     */
    fun removeAll(beliefs: BeliefBase<B>): RetrieveResult<B>

    fun update(belief: B): RetrieveResult<B>

    /**
     * Performs unification between [B] and values in this [BeliefBase]
     */
    fun solve(belief: B): Solution
}
