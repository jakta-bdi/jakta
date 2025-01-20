package it.unibo.jakta.perception

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.perception.impl.PerceptionImpl

/** Component of a BDI Agent that let it perceive the environment. */
interface Perception {

    /**
     * Operation done by a BDI Agent at each reasoning cycle iteration.
     * @return a [ASBeliefBase] that describes the environment where the agent is situated.
     */
    fun percept(): ASBeliefBase

    companion object {
        fun empty(): Perception = PerceptionImpl()

        fun of(belief: ASBelief, vararg beliefs: ASBelief): Perception = of(listOf(belief, *beliefs))

        fun of(beliefs: Iterable<ASBelief>): Perception = PerceptionImpl(beliefs)
    }
}
