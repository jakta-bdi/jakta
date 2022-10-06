package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.collections.RetrieveResult

internal class BeliefBaseImpl (override val beliefs: ClauseMultiSet): BeliefBase {
    override fun add(belief: Belief) =
        when (beliefs.count(belief)) {
            // There's no Belief that unify the param inside the MultiSet, so it's inserted
            0L -> BeliefBaseImpl(beliefs.add(belief))
            // There are Beliefs that unify the param, so the belief it's not inserted
            else -> this
        }

    override fun addAll(beliefs: Iterable<Belief>): BeliefBase {
        var bSet: BeliefBase = this
        beliefs.forEach { bSet = add(it) }
        return bSet
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?) = beliefs == other

    override fun retrieve(belief: Belief): RetrieveResult<out ClauseMultiSet> = beliefs.retrieve(belief)

    override fun retrieveAll(belief: Belief): RetrieveResult<out ClauseMultiSet> = beliefs.retrieveAll(belief)

    // DA TESTARE BENE - NON SONO SICURA FUNZIONI
    override fun remove(belief: Belief): BeliefBase = BeliefBase.of(beliefs.filter { it != belief })

}