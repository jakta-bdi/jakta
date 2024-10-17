package it.unibo.jakta.perception

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.perception.impl.PerceptionImpl

/** Component of a BDI Agent that let it perceive the environment. */
interface Perception {

    /**
     * Operation done by a BDI Agent at each reasoning cycle iteration.
     * @return a [BeliefBase] that describes the environment where the agent is situated.
     */
    fun percept(): BeliefBase

    companion object {
        fun empty(): Perception = PerceptionImpl()

        fun of(belief: Belief, vararg beliefs: Belief): Perception = of(listOf(belief, *beliefs))

        fun of(beliefs: Iterable<Belief>): Perception = PerceptionImpl(beliefs)
    }
}
