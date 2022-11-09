package io.github.anitvam.agents.bdi.reasoning.perception

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.reasoning.perception.impl.EmptyPerception

/** Component of a BDI Agent that let it perceive the environment. */
interface Perception {

    /**
     * Operation done by a BDI Agent at each reasoning cycle iteration.
     * @return a [BeliefBase] that describes the environment where the agent is situated.
     */
    fun percept(): BeliefBase

    companion object {
        /** @return a [EmptyPerception] */
        fun empty(): Perception = EmptyPerception()
    }
}
