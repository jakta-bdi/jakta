package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.Jacop
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefUpdate
import io.github.anitvam.agents.bdi.beliefs.RetrieveResult
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.classic.stdlib.DefaultBuiltins
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.theory.Theory

internal class BeliefBaseImpl(private val beliefs: ClauseMultiSet) : BeliefBase {

    private val operatorExtension = Theory.of(
        Jacop.parseClause("&(A, B) :- A, B"),
        Jacop.parseClause("|(A, _) :- A"),
        Jacop.parseClause("|(_, B) :- B"),
        Jacop.parseClause("~(X) :- not(X)"),
    )

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

    override fun equals(other: Any?) = beliefs == other

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
            .runtime(Runtime.of(DefaultBuiltins))
            .staticKb(operatorExtension + Theory.of(beliefs))
            .build()
            .solveOnce(struct)

    override fun solve(belief: Belief): Solution = solve(belief.rule.head)

    override fun toString(): String =
        beliefs.joinToString { Belief.from(it.castToRule()).toString() }
}
