package io.github.anitvam.agents.bdi.intentions.impl

import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionID
import io.github.anitvam.agents.bdi.intentions.IntentionPool

internal data class IntentionPoolImpl(
    val from: Map<IntentionID, Intention> = emptyMap(),
) : IntentionPool, LinkedHashMap<IntentionID, Intention>(from) {

    override fun update(intention: Intention): IntentionPool =
        if (intention.recordStack.isEmpty()) {
            IntentionPoolImpl(this - intention.id)
        } else {
            IntentionPoolImpl(this + Pair(intention.id, intention))
        }

    override fun nextIntention(): Intention = this.entries.iterator().next().value

    override fun pop(): IntentionPool = IntentionPoolImpl(this - nextIntention().id)
}
