package io.github.anitvam.agents.bdi.beliefs.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.collections.RetrieveResult
import it.unibo.tuprolog.core.Clause

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
        beliefs.forEachBelief { bSet = add(it, onAdditionPerformed) }
        return bSet
    }

    override fun hashCode() = beliefs.hashCode()

    override fun equals(other: Any?) = beliefs == other

    override fun forEachBelief(action: (Belief) -> Unit) = beliefs.forEach { action(it as Belief) }
    // Downcast orribile. Tengo ClauseMultiSet o Collection<Belief>??? In teoria però so già che dentro avrò solo Belief perchè li vincolo da interfaccia

    override fun filter(filter: (Belief) -> Boolean): BeliefBase {
        var bSet: BeliefBase = this
        forEachBelief { if (!filter(it)) bSet = remove(it) }
        return bSet
    }
    override fun retrieve(belief: Belief): RetrieveResult<out ClauseMultiSet> = beliefs.retrieve(belief)

    override fun retrieveAll(belief: Belief): RetrieveResult<out ClauseMultiSet> = beliefs.retrieveAll(belief)

    // DA TESTARE BENE - NON SONO SICURA FUNZIONI
    override fun remove(belief: Belief, onRemovalPerformed: (removedBelief: Belief) -> Unit): BeliefBase {
        beliefs.filter { it == belief }.forEach { onRemovalPerformed(it as Belief) }
        return filter { it != belief }
    }

    override fun removeAll(beliefs: BeliefBase, onRemovalPerformed: (removedBelief: Belief) -> Unit): BeliefBase {
        var bSet: BeliefBase = this
        beliefs.forEachBelief { bSet = remove(it, onRemovalPerformed) }
        return bSet
    }

    override fun contains(belief: Belief): Boolean = beliefs.contains(belief)

}