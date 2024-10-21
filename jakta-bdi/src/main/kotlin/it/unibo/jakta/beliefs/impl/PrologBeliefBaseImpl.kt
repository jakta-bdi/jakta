package it.unibo.jakta.beliefs.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.beliefs.BeliefUpdate
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.resolution.Solution
import it.unibo.jakta.resolution.toJaktaSolution
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import it.unibo.tuprolog.solve.Solution as TuprologSolution

internal class PrologBeliefBaseImpl private constructor(
    private val beliefs: ClauseMultiSet,
    override val delta: List<BeliefUpdate<PrologBelief>> = emptyList(),
) : PrologBeliefBase {

    constructor() : this(ClauseMultiSet.empty(Unificator.default))

    override operator fun plus(belief: PrologBelief) = when (beliefs.count(belief.content)) {
        // There's no Belief that unify the param inside the MultiSet, so it's inserted
        0L -> PrologBeliefBaseImpl(beliefs.add(belief.content), listOf(BeliefUpdate.addition(belief)))
        // There are Beliefs that unify the param, so the belief it's not inserted
        else -> this
    }

    override operator fun plus(beliefs: PrologBeliefBase): PrologBeliefBase {
        var bb: PrologBeliefBase = this
        beliefs.forEach { bb += it }
        return bb
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PrologBeliefBaseImpl
        return beliefs == other.beliefs
    }

    override operator fun minus(belief: PrologBelief): PrologBeliefBase {
        return if (beliefs.count(belief.content) > 0) {
            PrologBeliefBaseImpl(
                ClauseMultiSet.of(Unificator.default, beliefs.filter { it != belief }),
                listOf(BeliefUpdate.removal(first { it == belief })),
            )
        } else {
            this
        }
    }

    override operator fun minus(beliefs: PrologBeliefBase): PrologBeliefBase {
        var bb: PrologBeliefBase = this
        beliefs.forEach { bb -= it }
        return bb
    }

    override fun update(belief: PrologBelief): PrologBeliefBase {
        val element = beliefs.find { it.head?.functor == belief.content.head.functor }
        return if (element != null) {
            this - (PrologBelief.from(element.head!!)) + belief
        } else {
            this
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

    override fun resetDelta(): PrologBeliefBase = PrologBeliefBaseImpl(beliefs)

    override fun isEmpty(): Boolean = beliefs.isEmpty()

    override fun iterator(): Iterator<PrologBelief> =
        beliefs.filterIsInstance<Rule>().map { PrologBelief.from(it) }.iterator()

    override val size = beliefs.size

    override fun contains(element: PrologBelief) = beliefs.contains(element.content)

    override fun containsAll(elements: Collection<PrologBelief>) = beliefs.containsAll(elements.map { it.content })

    override fun toString(): String =
        beliefs.joinToString { PrologBelief.from(it.castToRule()).toString() }

    companion object {
        private val operatorExtension = Theory.of(
            Jakta.parseClause("&(A, B) :- A, B"),
            Jakta.parseClause("|(A, _) :- A"),
            Jakta.parseClause("|(_, B) :- B"),
            Jakta.parseClause("~(X) :- not(X)"),
        )
    }
}
