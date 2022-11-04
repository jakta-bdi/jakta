package io.github.anitvam.agents.bdi.intentions.impl

import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionID
import io.github.anitvam.agents.bdi.intentions.IntentionPool

internal class IntentionPoolImpl(
    from : Map<IntentionID, Intention> = emptyMap(),
) : IntentionPool, LinkedHashMap<IntentionID, Intention>(from) {

    override fun update(intention: Intention) =
        IntentionPoolImpl(this + Pair(intention.id, intention))

    override fun nextIntention() : Intention = this.entries.iterator().next().value

    override fun pop(): IntentionPool = IntentionPoolImpl(this - nextIntention().id)

    override operator fun minus(id: IntentionID) : IntentionPool = IntentionPoolImpl(this - id)

}

