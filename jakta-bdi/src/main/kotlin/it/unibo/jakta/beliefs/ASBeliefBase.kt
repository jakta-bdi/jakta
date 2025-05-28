package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.ASBeliefBaseImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution

/** A BDI Agent's collection of [ASBelief] */
interface ASBeliefBase : BeliefBase<ASBelief, Struct, Solution>

interface ASMutableBeliefBase : ASBeliefBase {

    /**
     * @return the immutable [BeliefBase] version of this instance.
     */
    fun snapshot(): ASBeliefBase

    /**
     * Adds a [Belief] to this [BeliefBase], if not present.
     * @param belief: the [Belief] to be added.
     * @return true if the [BeliefBase] was changed as the result of the operation.
     **/
    fun add(belief: ASBelief): Boolean

    /**
     * Adds all the given beliefs into this [BeliefBase], if not present.
     * @param beliefs: the [BeliefBase] to be added.
     * @return true if the [BeliefBase] was changed as the result of the operation.
     **/
    fun addAll(beliefs: ASBeliefBase): Boolean {
        var result = false
        for (b in beliefs) {
            if (add(b as ASBelief)) result = true // TODO(CAST)
        }
        return result
    }

    /**
     * Removes a [Belief] from the [BeliefBase], if present.
     * @param belief: the [Belief] to be removed
     * @return true if the [BeliefBase] was changed as the result of the operation.
     */
    fun remove(belief: ASBelief): Boolean

    /**
     * Removes all the specified beliefs from this [BeliefBase], if present.
     * @param beliefs: beliefs to be removed
     * @return true if the [BeliefBase] was changed as the result of the operation.
     */
    fun removeAll(beliefs: ASBeliefBase): Boolean {
        var result = false
        for (b in beliefs) {
            if (remove(b as ASBelief)) result = true // TODO(CAST)
        }
        return result
    }

    /**
     * Updates the content of the [BeliefBase].
     * @param belief the [Belief] to update
     * @return true if the [BeliefBase] was changed as the result of the operation.
     *
     */
    fun update(belief: ASBelief): Boolean

    fun update(beliefBase: ASBeliefBase): Boolean

    companion object {
        /** @return an empty [ASMutableBeliefBase] */
        fun empty(): ASMutableBeliefBase = ASBeliefBaseImpl()

        /**
         * Generates a [ASBeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [ASBeliefBase] will be composed of
         * @return the new [ASBeliefBase]
         */
        fun of(beliefs: Iterable<ASBelief>): ASMutableBeliefBase = ASBeliefBaseImpl(beliefs.toMutableList())

        fun of(vararg beliefs: ASBelief): ASMutableBeliefBase = of(beliefs.asList())
    }
}
