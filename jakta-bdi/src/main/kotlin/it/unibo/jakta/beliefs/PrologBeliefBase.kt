package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.PrologBeliefBaseImpl
import it.unibo.tuprolog.core.Struct

/** A BDI Agent's collection of [PrologBelief] */
interface PrologBeliefBase : BeliefBase<Struct, PrologBelief, PrologBeliefBase> {

    fun select(query: PrologBelief): PrologBeliefBase

    interface PrologMutableBeliefBase : BeliefBase.MutableBeliefBase<Struct, PrologBelief, PrologBeliefBase> {
        /**
         * Updates the content of the [BeliefBase].
         * @param belief the [Belief] to update
         * @return true if the [BeliefBase] was changed as the result of the operation.
         *
         */
        fun update(belief: PrologBelief): Boolean
    }

    companion object {
        /** @return an empty [PrologBeliefBase] */
        fun empty(): PrologBeliefBase = PrologBeliefBaseImpl()

        /**
         * Generates a [PrologBeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [PrologBeliefBase] will be composed of
         * @return the new [PrologBeliefBase]
         */
        fun of(beliefs: Iterable<PrologBelief>): PrologBeliefBase {
            var bb = empty()
            beliefs.forEach { bb += it }
            return bb
        }

        fun of(vararg beliefs: PrologBelief): PrologBeliefBase =
            of(beliefs.asList())
    }
}

// Posso fare una select di una mutable bb ???
