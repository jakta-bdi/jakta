package it.unibo.jakta.intentions.impl

import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionID
import it.unibo.jakta.intentions.IntentionPool

internal data class IntentionPoolImpl(
    val from: Map<IntentionID, ASIntention> = emptyMap(),
) : ASIntentionPool, LinkedHashMap<IntentionID, ASIntention>(from) {

    override fun updateIntention(intention: Intention): IntentionPool =
        IntentionPoolImpl(this + Pair(intention.id, intention))

    override fun nextIntention(): Intention = this.entries.iterator().next().value

    override fun pop(): IntentionPool = IntentionPoolImpl(this - nextIntention().id)

    override fun deleteIntention(intentionID: IntentionID): IntentionPool =
        IntentionPoolImpl(this - intentionID)

    override fun toString(): String = from.values.joinToString(separator = "\n")
}
