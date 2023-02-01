package io.github.anitvam.agents.bdi.perception

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.perception.impl.PerceptionImpl

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
