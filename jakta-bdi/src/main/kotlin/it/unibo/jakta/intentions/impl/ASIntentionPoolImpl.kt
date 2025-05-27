package it.unibo.jakta.intentions.impl

import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.ASMutableIntentionPool
import it.unibo.jakta.intentions.IntentionID

internal class ASIntentionPoolImpl(
    from: MutableMap<IntentionID, ASIntention> = mutableMapOf(),
) : ASIntentionPool, ASMutableIntentionPool, MutableMap<IntentionID, ASIntention> by from {

    override fun updateIntention(intention: ASIntention): Boolean {
        when (intention.recordStack.isEmpty()) {
            true -> this.remove(intention.id).also{println(this)}
            else -> this.put(intention.id, intention) ?: return false
        }
        return true
    }

    override fun nextIntention(): ASIntention = this.values.first()

    override fun pop(): ASIntention? = this.remove(nextIntention().id)

    override fun deleteIntention(intentionID: IntentionID): ASIntention? = this.remove(intentionID)

    override fun snapshot(): ASIntentionPool =
        ASIntentionPoolImpl(mutableMapOf(*this.entries.map { it.key to it.value }.toTypedArray()))

    override fun toString(): String = this.values.joinToString(separator = "\n")
}
