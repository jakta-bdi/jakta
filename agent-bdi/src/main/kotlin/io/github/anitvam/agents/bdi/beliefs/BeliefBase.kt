package io.github.anitvam.agents.bdi.beliefs

import io.github.anitvam.agents.bdi.Percept
import io.github.anitvam.agents.bdi.beliefs.impl.BeliefBaseImpl
import io.github.anitvam.agents.bdi.reasoning.perception.impl.EmptyPerception
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.collections.RetrieveResult
import it.unibo.tuprolog.theory.Theory

/**
 * A BDI Agent's collection of [Beliefs]
 */
interface BeliefBase {

    val beliefs: ClauseMultiSet

    /**
     * Adds a [Belief] to this [BeliefBase]
     * @param belief: the [Belief] to be added
     * @param onAdditionPerformed: the callback that will be invoked when the addition is performed, it takes the
     * added belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     **/
    fun add(belief: Belief, onAdditionPerformed: (addedBelief: Belief) -> Unit = {}): BeliefBase

    /**
     * Adds all the given [Belief] to this [BeliefBase]
     * @param beliefs: the collection of [Belief] to be added
     * @param onAdditionPerformed: the callback that will be invoked when each addition is performed, it takes the
     * added belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     **/
    fun addAll(beliefs: Iterable<Belief>, onAdditionPerformed: (addedBelief: Belief) -> Unit = {}): BeliefBase

    /** Retrieves the first unifying [Belief] from this [BeliefBase] as a [RetrieveResult]**/
    fun retrieve(belief: Belief): RetrieveResult<out ClauseMultiSet>

    /** Retrieves all the unifying [Belief] from this [BeliefBase] as a [RetrieveResult]**/
    fun retrieveAll(belief: Belief): RetrieveResult<out ClauseMultiSet>

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @param belief: the [Belief] to be removed
     * @param onRemovalPerformed: the callback that will be invoked when the removal is performed, it takes the
     * added belief as a parameter. By default, it does nothing.
     * @return the updated [BeliefBase]
     */
    fun remove(belief: Belief, onRemovalPerformed: (removedBelief: Belief) -> Unit = {}): BeliefBase

    companion object {
        fun empty(): BeliefBase = BeliefBaseImpl(ClauseMultiSet.empty())
        fun of(beliefs: Iterable<Belief>): BeliefBase = BeliefBaseImpl(ClauseMultiSet.of(beliefs))
    }
}
