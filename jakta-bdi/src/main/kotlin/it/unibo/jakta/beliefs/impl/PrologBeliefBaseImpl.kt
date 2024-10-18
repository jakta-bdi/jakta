package it.unibo.jakta.beliefs.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.beliefs.BeliefUpdate
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.beliefs.RetrieveResult
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
) : PrologBeliefBase {

    constructor() : this(ClauseMultiSet.empty(Unificator.default))

    override fun add(belief: PrologBelief) = when (beliefs.count(belief.content)) {
        // There's no Belief that unify the param inside the MultiSet, so it's inserted
        0L -> RetrieveResult(listOf(BeliefUpdate.addition(belief)), PrologBeliefBaseImpl(beliefs.add(belief.content)))
        // There are Beliefs that unify the param, so the belief it's not inserted
        else -> RetrieveResult(emptyList(), this)
    }

    override fun addAll(beliefs: PrologBeliefBase): RetrieveResult<PrologBelief, PrologBeliefBase> {
        var addedBeliefs = emptyList<BeliefUpdate<PrologBelief>>()
        var bb: PrologBeliefBase = this
        beliefs.forEach {
            val rr = bb.add(it)
            addedBeliefs = addedBeliefs + rr.modifiedBeliefs
            bb = rr.updatedBeliefBase
        }
        return RetrieveResult(addedBeliefs, bb)
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PrologBeliefBaseImpl
        return beliefs == other.beliefs
    }

    override fun remove(belief: PrologBelief): RetrieveResult<PrologBelief, PrologBeliefBase> {
        return if (beliefs.count(belief.content) > 0) {
            RetrieveResult(
                listOf(BeliefUpdate.removal(first { it == belief })),
                PrologBeliefBase.of(filter { it != belief }),
            )
        } else {
            RetrieveResult(listOf(), this)
        }
    }

    override fun update(belief: PrologBelief): RetrieveResult<PrologBelief, PrologBeliefBase> {
        val element = beliefs.find { it.head?.functor == belief.content.head.functor }
        return if (element != null) {
            var retrieveResult = remove(PrologBelief.from(element.head!!))
            retrieveResult = retrieveResult.updatedBeliefBase.add(belief)
            RetrieveResult(listOf(), retrieveResult.updatedBeliefBase)
        } else {
            RetrieveResult(listOf(), this)
        }
    }

    override fun removeAll(beliefs: PrologBeliefBase): RetrieveResult<PrologBelief, PrologBeliefBase> {
        var removedBeliefs = emptyList<BeliefUpdate<PrologBelief>>()
        var bb: PrologBeliefBase = this
        beliefs.forEach {
            val rr = bb.remove(it)
            removedBeliefs = removedBeliefs + rr.modifiedBeliefs
            bb = rr.updatedBeliefBase
        }
        return RetrieveResult(removedBeliefs, bb)
    }

    override fun iterator(): Iterator<PrologBelief> =
        beliefs.filterIsInstance<Rule>().map { PrologBelief.from(it) }.iterator()

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

    companion object {
        private val operatorExtension = Theory.of(
            Jakta.parseClause("&(A, B) :- A, B"),
            Jakta.parseClause("|(A, _) :- A"),
            Jakta.parseClause("|(_, B) :- B"),
            Jakta.parseClause("~(X) :- not(X)"),
        )
    }
}
