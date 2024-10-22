package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.BeliefBase.Update

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<in Query : Any, out Belief, SelfType : BeliefBase<Query, Belief, SelfType>> : Collection<Belief> {

    /**
     * Performs unification between [B] and values in this [BeliefBase]
     */
    fun select(query: Query): SelfType

    // TODO: this may be a container of multiple updates that happen atomically
    sealed interface Update<Belief> {
        val diff: Belief

        data class Addition<Belief>(override val diff: Belief) : Update<Belief>
        data class Removal<Belief>(override val diff: Belief) : Update<Belief>
    }
}

interface MutableBeliefBase<Query : Any, Belief, ImmBB : BeliefBase<Query, Belief, ImmBB>> : Collection<Belief> {

    /**
     * List of [Update] that populated the current [BeliefBase].
     **/
    var delta: List<Update<Belief>>

    /**
     * @return the immutable [BeliefBase] version of this instance.
     */
    fun snapshot(): ImmBB

    /**
     * Adds a [Belief] to this [BeliefBase], if not present.
     * @param belief: the [Belief] to be added.
     * @return true if the [BeliefBase] was changed as the result of the operation.
     **/
    fun add(belief: Belief): Boolean

    /**
     * Adds all the given beliefs into this [BeliefBase], if not present.
     * @param beliefs: the [BeliefBase] to be added.
     * @return true if the [BeliefBase] was changed as the result of the operation.
     **/
    fun addAll(beliefs: ImmBB): Boolean {
        var result = false
        for (b in beliefs) {
            if (add(b)) result = true
        }
        return result
    }

    /**
     * Removes a [Belief] from the [BeliefBase], if present.
     * @param belief: the [Belief] to be removed
     * @return true if the [BeliefBase] was changed as the result of the operation.
     */
    fun remove(belief: Belief): Boolean

    /**
     * Removes all the specified beliefs from this [BeliefBase], if present.
     * @param beliefs: beliefs to be removed
     * @return true if the [BeliefBase] was changed as the result of the operation.
     */
    fun removeAll(beliefs: ImmBB): Boolean {
        var result = false
        for (b in beliefs) {
            if (remove(b)) result = true
        }
        return result
    }
}
