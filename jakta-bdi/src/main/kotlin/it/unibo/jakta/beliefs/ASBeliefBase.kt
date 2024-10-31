package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.ASBeliefBaseImpl
import it.unibo.tuprolog.core.Struct

/** A BDI Agent's collection of [ASBelief] */
interface ASBeliefBase : BeliefBase<Struct, ASBelief> {

    fun select(query: ASBelief): List<ASBelief>
}

interface ASMutableBeliefBase : MutableBeliefBase<Struct, ASBelief, ASBeliefBase> {
    /**
     * Updates the content of the [BeliefBase].
     * @param belief the [Belief] to update
     * @return true if the [BeliefBase] was changed as the result of the operation.
     *
     */
    fun update(belief: ASBelief): Boolean

    companion object {
        /** @return an empty [ASMutableBeliefBase] */
        fun empty(): ASMutableBeliefBase = ASBeliefBaseImpl()

        /**
         * Generates a [ASBeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [ASBeliefBase] will be composed of
         * @return the new [ASBeliefBase]
         */
        fun of(beliefs: Iterable<ASBelief>): ASMutableBeliefBase {
            val bb = empty()
            beliefs.forEach { bb.add(it) }
            return bb
        }

        fun of(vararg beliefs: ASBelief): ASMutableBeliefBase = of(beliefs.asList())
    }
}
