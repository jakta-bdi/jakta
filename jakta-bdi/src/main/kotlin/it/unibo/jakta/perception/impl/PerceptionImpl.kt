package it.unibo.jakta.perception.impl

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.perception.Perception

internal data class PerceptionImpl(
    val beliefs: Iterable<Belief> = emptyList(),
) : Perception {
    override fun percept(): BeliefBase = BeliefBase.of(beliefs)
}
