package it.unibo.jakta.agents.bdi.perception.impl

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.perception.Perception

internal data class PerceptionImpl(
    val beliefs: Iterable<Belief> = emptyList(),
) : Perception {
    override fun percept(): BeliefBase = BeliefBase.of(beliefs)
}
