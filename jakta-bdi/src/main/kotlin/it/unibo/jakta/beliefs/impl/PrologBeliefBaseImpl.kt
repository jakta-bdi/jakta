package it.unibo.jakta.beliefs.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.resolution.toJaktaSolution
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import it.unibo.tuprolog.solve.Solution as TuprologSolution

internal data class PrologBeliefBaseImpl(
    private var beliefs: ClauseMultiSet,
    override var delta: List<BeliefBase.Update<PrologBelief>> = emptyList(),
) : PrologBeliefBase.PrologMutableBeliefBase, PrologBeliefBase {

    constructor() : this(ClauseMultiSet.empty(Unificator.default))

    override fun snapshot(): PrologBeliefBase = TODO()

    override fun select(query: Struct): PrologBeliefBase =
        PrologBeliefBase.of(
            PrologBelief.of(
                Solver.prolog.newBuilder()
                    .flag(Unknown, Unknown.FAIL)
                    .staticKb(operatorExtension + Theory.of(beliefs))
                    .flag(TrackVariables) { ON }
                    .build()
                    .solveOnce(query)
                    .solvedQuery,
            ),
        )

    override fun select(query: PrologBelief): PrologBeliefBase {
        TODO("Not yet implemented")
    }

    override fun update(belief: PrologBelief): Boolean {
        val element = beliefs.find { it.head?.functor == belief.head.functor }
        return if (element != null) {
            beliefs = ClauseMultiSet.of(Unificator.default, beliefs.filter { it != belief }).add(belief)
            true
        } else {
            false
        }
    }

    override fun remove(belief: PrologBelief): Boolean = when (beliefs.count(belief)) {
        0L -> false
        else -> true.also {
            val match = beliefs.filterIsInstance<PrologBelief>().first { it == belief }
            delta += BeliefBase.Update.Removal(match)
            beliefs = ClauseMultiSet.of(Unificator.default, beliefs.filter { it != belief })
        }
    }

    override fun add(belief: PrologBelief): Boolean = when (beliefs.count(belief)) {
        // There's no Belief that unify the param inside the MultiSet, so it's inserted
        0L -> true.also {
            beliefs.add(belief)
            delta += BeliefBase.Update.Addition(belief)
        }
        // There are Beliefs that unify the param, so the belief it's not inserted
        else -> false
    }

    override val size = beliefs.size

    override fun isEmpty() = beliefs.isEmpty()

    override fun iterator() = beliefs.filterIsInstance<PrologBelief>().iterator()

    override fun containsAll(elements: Collection<PrologBelief>) = beliefs.containsAll(elements)

    override fun contains(element: PrologBelief) = beliefs.contains(element)

    companion object {
        private val operatorExtension = Theory.of(
            Jakta.parseClause("&(A, B) :- A, B"),
            Jakta.parseClause("|(A, _) :- A"),
            Jakta.parseClause("|(_, B) :- B"),
            Jakta.parseClause("~(X) :- not(X)"),
        )
    }
}

override fun solve(struct: Struct): Solution<TuprologSolution> =
    Solver.prolog.newBuilder()
        .flag(Unknown, Unknown.FAIL)
        .staticKb(operatorExtension + Theory.of(beliefs))
        .flag(TrackVariables) { ON }
        .build()
        .solveOnce(struct)
        .toJaktaSolution()

override fun solve(belief: PrologBelief): Solution<TuprologSolution> = solve(belief.content.head)

override fun toString(): String =
    beliefs.joinToString { PrologBelief.from(it.castToRule()).toString() }
