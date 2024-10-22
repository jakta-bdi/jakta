package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.PrologBeliefBaseImpl
import it.unibo.tuprolog.core.Struct

/** A BDI Agent's collection of [PrologBelief] */
interface PrologBeliefBase : BeliefBase<Struct, PrologBelief, PrologBeliefBase> {

    fun select(query: PrologBelief): PrologBeliefBase
}

interface PrologMutableBeliefBase : MutableBeliefBase<Struct, PrologBelief, PrologBeliefBase> {
    /**
     * Updates the content of the [BeliefBase].
     * @param belief the [Belief] to update
     * @return true if the [BeliefBase] was changed as the result of the operation.
     *
     */
    fun update(belief: PrologBelief): Boolean

    companion object {
        /** @return an empty [PrologMutableBeliefBase] */
        fun empty(): PrologMutableBeliefBase = PrologBeliefBaseImpl()

        /**
         * Generates a [PrologBeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [PrologBeliefBase] will be composed of
         * @return the new [PrologBeliefBase]
         */
        fun of(beliefs: Iterable<PrologBelief>): PrologMutableBeliefBase {
            val bb = empty()
            beliefs.forEach { bb.add(it) }
            return bb
        }

        fun of(vararg beliefs: PrologBelief): PrologMutableBeliefBase = of(beliefs.asList())
    }
}
