package io.github.anitvam.agents.bdi.perception.impl

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.perception.Perception

/** [Perception] that returns an empty [BeliefBase] */
internal class EmptyPerception : Perception {
    override fun percept(): BeliefBase = BeliefBase.empty()
}
