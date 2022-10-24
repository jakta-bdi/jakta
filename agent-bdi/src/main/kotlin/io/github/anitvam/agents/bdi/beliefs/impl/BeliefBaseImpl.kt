package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.beliefs.RetrieveResult
import it.unibo.tuprolog.collections.ClauseMultiSet

internal class BeliefBaseImpl(private val beliefs: ClauseMultiSet) : BeliefBase {
    override fun add(belief: Belief) = when (beliefs.count(belief)) {
            // There's no Belief that unify the param inside the MultiSet, so it's inserted
            0L -> RetrieveResult(listOf(belief), BeliefBaseImpl(beliefs.add(belief)))
            // There are Beliefs that unify the param, so the belief it's not inserted
            else -> RetrieveResult(emptyList(), this)
        }

    override fun addAll(beliefs: BeliefBase) : RetrieveResult {
        val addedBeliefs = emptyList<Belief>().toMutableList()
        var rr = RetrieveResult(addedBeliefs, this)
        beliefs.forEach {
            rr = add(it)
            addedBeliefs += rr.modifiedBeliefs
        }
        return RetrieveResult(addedBeliefs, rr.updatedBeliefBase)
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?) = beliefs == other

    // DA TESTARE BENE - NON SONO SICURA FUNZIONI
    override fun remove(belief: Belief) = RetrieveResult(filter { it == belief }, BeliefBase.of(filter { it != belief }))

    override fun removeAll(beliefs: BeliefBase): RetrieveResult {
        val removedBeliefs = emptyList<Belief>().toMutableList()
        var rr = RetrieveResult(removedBeliefs, this)
        beliefs.forEach {
            rr = remove(it)
            removedBeliefs += rr.modifiedBeliefs
        }
        return RetrieveResult(removedBeliefs, rr.updatedBeliefBase)
    }

    override fun iterator(): Iterator<Belief> = beliefs.filterIsInstance<Belief>().iterator()
}