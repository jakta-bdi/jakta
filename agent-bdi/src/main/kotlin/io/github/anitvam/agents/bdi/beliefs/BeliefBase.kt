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

    /** Adds a [Belief] to this [BeliefBase] **/
    fun add(belief: Belief): BeliefBase

    /** Adds all the given [Belief] to this [BeliefBase] **/
    fun addAll(beliefs: Iterable<Belief>): BeliefBase

    /** Retrieves the first unifying [Belief] from this [BeliefBase] as a [RetrieveResult]**/
    fun retrieve(belief: Belief): RetrieveResult<out ClauseMultiSet>

    /** Retrieves all the unifying [Belief] from this [BeliefBase] as a [RetrieveResult]**/
    fun retrieveAll(belief: Belief): RetrieveResult<out ClauseMultiSet>

    /**
     * Removes a [Belief] from the [BeliefBase]
     * @return the updated [BeliefBase]
     */
    fun remove(belief: Belief): BeliefBase

    companion object {
        fun empty(): BeliefBase = BeliefBaseImpl(ClauseMultiSet.empty())
        fun of(beliefs: Iterable<Belief>): BeliefBase = BeliefBaseImpl(ClauseMultiSet.of(beliefs))
    }
}
