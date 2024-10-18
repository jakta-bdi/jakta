package it.unibo.jakta.beliefs

import it.unibo.jakta.beliefs.impl.PrologBeliefBaseImpl
import it.unibo.jakta.resolution.Solution
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution as TuprologSolution

/** A BDI Agent's collection of [PrologBelief] */
interface PrologBeliefBase : BeliefBase<PrologBelief, PrologBeliefBase> {

    fun solve(struct: Struct): Solution<TuprologSolution>

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
            beliefs.forEach { bb = bb.add(it).updatedBeliefBase }
            return bb
        }

        fun of(vararg beliefs: PrologBelief): PrologBeliefBase =
            of(beliefs.asList())
    }
}
