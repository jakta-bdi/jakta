package it.unibo.jakta.beliefs

import it.unibo.jakta.resolution.Solution

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<B : Belief<*>, BB : BeliefBase<B, BB>> : Collection<B> {

    /**
     * List of [BeliefUpdate] that populated the current [BeliefBase].
     **/
    val delta: List<BeliefUpdate<B>>

    /**
     * Resets the [BeliefBase] operations.
     * @return a [BeliefBase] where the delta list is empty.
     */
    fun resetDelta(): BB

    /**
     * Adds a [Belief] to this [BeliefBase]
     * @param belief: the [Belief] to be added
     * @return the new [BeliefBase] and the added [Belief]
     **/
    operator fun plus(belief: B): BB

    /**
     * Adds all the given beliefs into this [BeliefBase]
     * @param beliefs: beliefs to be added
     * @return the new [BeliefBase] and the added [Belief]s
     **/
    operator fun plus(beliefs: BB): BB

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @param belief: the [Belief] to be removed
     * @return the new [BeliefBase] and the removed [Belief]
     */
    operator fun minus(belief: B): BB

    /**
     * Removes all the specified beliefs from this [BeliefBase]
     * @param beliefs: beliefs to be removed
     * @return the new [BeliefBase] and the removed [Belief]s
     */
    operator fun minus(beliefs: BB): BB

    /**
     * Performs unification between [B] and values in this [BeliefBase]
     */
    fun solve(belief: B): Solution<*>
}
