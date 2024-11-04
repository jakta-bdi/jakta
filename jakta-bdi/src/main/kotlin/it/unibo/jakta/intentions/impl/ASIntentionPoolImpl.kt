package it.unibo.jakta.intentions.impl

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionID
import it.unibo.tuprolog.core.Struct

internal data class ASIntentionPoolImpl(
    val from: Map<IntentionID, Intention<Struct, ASBelief>> = emptyMap(),
) : ASIntentionPool, LinkedHashMap<IntentionID, Intention<Struct, ASBelief>>(from) {

    override fun updateIntention(intention: Intention<Struct, ASBelief>): ASIntentionPool {
        return if (intention is ASIntention){
            ASIntentionPoolImpl(this + Pair(intention.id, intention))
        } else this
    }

    override fun nextIntention(): ASIntention = this.values.filterIsInstance<ASIntention>().iterator().next()

    override fun pop(): ASIntentionPool = ASIntentionPoolImpl(this - nextIntention().id)

    override fun deleteIntention(intentionID: IntentionID): ASIntentionPool =
        ASIntentionPoolImpl(this - intentionID)

    override fun toString(): String = from.values.joinToString(separator = "\n")
}
