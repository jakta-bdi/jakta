package it.unibo.jakta.perception.impl

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.perception.Perception

internal data class PerceptionImpl(val beliefs: Iterable<ASBelief> = emptyList()) : Perception {
    override fun percept(): ASBeliefBase = MutableBeliefBase.of(beliefs).snapshot()
}
