package io.github.anitvam.agents.bdi.intentions.impl

import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionID
import it.unibo.tuprolog.core.Substitution

internal class IntentionImpl(
    override val recordStack: List<ActivationRecord>,
    override val isSuspended: Boolean = false,
    override val id: IntentionID = IntentionID(),
) : Intention {

    override fun pop(): Intention =
        this.copy(recordStack = recordStack - recordStack.last())

    override fun push(activationRecord: ActivationRecord) =
        this.copy(recordStack = listOf(activationRecord) + recordStack)

    override fun applySubstitution(substitution: Substitution): Intention =
        this.copy(recordStack = recordStack.map { it.applySubstitution(substitution) })
}
