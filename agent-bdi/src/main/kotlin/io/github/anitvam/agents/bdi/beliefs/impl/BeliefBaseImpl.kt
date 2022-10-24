package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.collections.ClauseMultiSet

internal class BeliefBaseImpl(private val beliefs: ClauseMultiSet): BeliefBase {
    override fun add(belief: Belief, onAdditionPerformed: (addedBelief: Belief) -> Unit) =
        when (beliefs.count(belief)) {
            // There's no Belief that unify the param inside the MultiSet, so it's inserted
            0L -> {
                onAdditionPerformed(belief)
                BeliefBaseImpl(beliefs.add(belief))
            }
            // There are Beliefs that unify the param, so the belief it's not inserted
            else -> this
        }

    override fun addAll(beliefs: BeliefBase, onAdditionPerformed: (addedBelief: Belief) -> Unit): BeliefBase {
        var bSet: BeliefBase = this
        forEach { bSet = add(it, onAdditionPerformed) }
        return bSet
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?) = beliefs == other

    // DA TESTARE BENE - NON SONO SICURA FUNZIONI
    override fun remove(belief: Belief, onRemovalPerformed: (removedBelief: Belief) -> Unit): BeliefBase {
        filter { it == belief }.forEach { onRemovalPerformed(it) }
        return BeliefBase.of(filter { it != belief })
    }

    override fun removeAll(beliefs: BeliefBase, onRemovalPerformed: (removedBelief: Belief) -> Unit): BeliefBase {
        var bSet: BeliefBase = this
        beliefs.forEach { bSet = remove(it, onRemovalPerformed) }
        return bSet
    }

    override fun iterator(): Iterator<Belief> = beliefs.filterIsInstance<Belief>().iterator()
}