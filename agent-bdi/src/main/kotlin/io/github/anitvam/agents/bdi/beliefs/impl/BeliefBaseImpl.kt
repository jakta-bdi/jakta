package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.beliefs.*
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.theory.Theory

internal class BeliefBaseImpl(private val beliefs: ClauseMultiSet) : BeliefBase {
    override fun add(belief: Belief) = when (beliefs.count(belief)) {
            // There's no Belief that unify the param inside the MultiSet, so it's inserted
            0L -> RetrieveResult(listOf(BeliefUpdate.addition(belief)), BeliefBaseImpl(beliefs.add(belief)))
            // There are Beliefs that unify the param, so the belief it's not inserted
            else -> RetrieveResult(emptyList(), this)
        }

    override fun addAll(beliefs: BeliefBase) : RetrieveResult {
        var addedBeliefs = emptyList<BeliefUpdate>()
        var rr = RetrieveResult(addedBeliefs, this)
        beliefs.forEach {
            rr = add(it)
            addedBeliefs = addedBeliefs + rr.modifiedBeliefs
        }
        return RetrieveResult(addedBeliefs, rr.updatedBeliefBase)
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?) = beliefs == other

    // DA TESTARE BENE - NON SONO SICURA FUNZIONI
    override fun remove(belief: Belief) = RetrieveResult(listOf(BeliefUpdate.removal(first { it == belief })), BeliefBase.of(filter { it != belief }))

    override fun removeAll(beliefs: BeliefBase): RetrieveResult {
        var removedBeliefs = emptyList<BeliefUpdate>()
        var rr = RetrieveResult(removedBeliefs, this)
        beliefs.forEach {
            rr = remove(it)
            removedBeliefs = removedBeliefs + rr.modifiedBeliefs
        }
        return RetrieveResult(removedBeliefs, rr.updatedBeliefBase)
    }

    override fun iterator(): Iterator<Belief> = beliefs.filterIsInstance<Belief>().iterator()

    override fun solve(struct: Struct): Solution = Solver.prolog.solverOf(staticKb = Theory.of(beliefs)).solveOnce(struct)
}