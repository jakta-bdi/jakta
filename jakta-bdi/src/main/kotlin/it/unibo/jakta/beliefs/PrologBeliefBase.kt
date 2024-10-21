package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.PrologBeliefBaseImpl
import it.unibo.jakta.resolution.Solution
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution as TuprologSolution

/** A BDI Agent's collection of [PrologBelief] */
interface PrologBeliefBase : BeliefBase<Struct, Rule, PrologBeliefBase> {

    override fun solve(query: Struct): PrologBeliefBase

    /**
     * Updates the content of the [BeliefBase] without saving the change in delta variable.
     * @param belief the [Belief] to update
     * @return a [BeliefBase]
     */
    fun update(belief: PrologBelief): PrologBeliefBase

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
