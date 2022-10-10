package io.github.anitvam.agents.bdi.reasoning.perception.impl

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.reasoning.perception.Perception

/** [Perception] that returns an empty [BeliefBase] */
class EmptyPerception: Perception {
    override fun percept(): BeliefBase = BeliefBase.empty()
}