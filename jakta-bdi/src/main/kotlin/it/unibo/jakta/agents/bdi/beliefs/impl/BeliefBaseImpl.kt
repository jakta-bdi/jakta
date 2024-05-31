package it.unibo.jakta.agents.bdi.beliefs.impl

import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.beliefs.BeliefUpdate
import it.unibo.jakta.agents.bdi.beliefs.RetrieveResult
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

internal class BeliefBaseImpl private constructor(private val beliefs: ClauseMultiSet) : BeliefBase {

    constructor() : this(ClauseMultiSet.empty(Unificator.default))

    override fun add(belief: Belief) = when (beliefs.count(belief.rule)) {
        // There's no Belief that unify the param inside the MultiSet, so it's inserted
        0L -> RetrieveResult(listOf(BeliefUpdate.addition(belief)), BeliefBaseImpl(beliefs.add(belief.rule)))
        // There are Beliefs that unify the param, so the belief it's not inserted
        else -> RetrieveResult(emptyList(), this)
    }

    override fun addAll(beliefs: BeliefBase): RetrieveResult {
        var addedBeliefs = emptyList<BeliefUpdate>()
        var bb: BeliefBase = this
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
        other as BeliefBaseImpl
        return beliefs == other.beliefs
    }

    override fun remove(belief: Belief): RetrieveResult {
        return if (beliefs.count(belief.rule) > 0) {
            RetrieveResult(listOf(BeliefUpdate.removal(first { it == belief })), BeliefBase.of(filter { it != belief }))
        } else {
            RetrieveResult(listOf(), this)
        }
    }

    override fun update(belief: Belief): RetrieveResult {
        val element = beliefs.find { it.head?.functor == belief.rule.head.functor }
        return if (element != null) {
            var retrieveResult = remove(Belief.from(element.head!!))
            retrieveResult = retrieveResult.updatedBeliefBase.add(belief)
            RetrieveResult(listOf(), retrieveResult.updatedBeliefBase)
        } else {
            RetrieveResult(listOf(), this)
        }
    }

    override fun removeAll(beliefs: BeliefBase): RetrieveResult {
        var removedBeliefs = emptyList<BeliefUpdate>()
        var bb: BeliefBase = this
        beliefs.forEach {
            val rr = bb.remove(it)
            removedBeliefs = removedBeliefs + rr.modifiedBeliefs
            bb = rr.updatedBeliefBase
        }
        return RetrieveResult(removedBeliefs, bb)
    }

    override fun iterator(): Iterator<Belief> = beliefs.filterIsInstance<Rule>().map { Belief.from(it) }.iterator()

    override fun solve(struct: Struct): Solution =
        Solver.prolog.newBuilder()
            .flag(Unknown, Unknown.FAIL)
            .staticKb(operatorExtension + Theory.of(beliefs))
            .flag(TrackVariables) { ON }
            .build()
            .solveOnce(struct)

    override fun solve(belief: Belief): Solution = solve(belief.rule.head)

    override fun toString(): String =
        beliefs.joinToString { Belief.from(it.castToRule()).toString() }

    companion object {
        private val operatorExtension = Theory.of(
            Jakta.parseClause("&(A, B) :- A, B"),
            Jakta.parseClause("|(A, _) :- A"),
            Jakta.parseClause("|(_, B) :- B"),
            Jakta.parseClause("~(X) :- not(X)"),
        )
    }
}
