package it.unibo.jakta.intentions

import it.unibo.jakta.intentions.impl.ASIntentionPoolImpl

interface ASIntentionPool : Map<IntentionID, ASIntention> {
    fun nextIntention(): ASIntention
}

interface ASMutableIntentionPool : Map<IntentionID, ASIntention>, ASIntentionPool {
    fun updateIntention(intention: ASIntention): Boolean
    fun pop(): ASIntention?
    fun deleteIntention(intentionID: IntentionID): ASIntention?
    fun snapshot(): ASIntentionPool
}

object IntentionPoolStaticFactory {
    fun empty(): ASMutableIntentionPool = ASIntentionPoolImpl()

    fun of(intentions: Map<IntentionID, ASIntention>): ASMutableIntentionPool =
        ASIntentionPoolImpl(intentions.toMutableMap())

    fun of(vararg intentions: ASIntention): ASMutableIntentionPool = of(intentions.asList())

    fun of(intentions: List<ASIntention>): ASMutableIntentionPool =
        ASIntentionPoolImpl(intentions.associateBy { it.id }.toMutableMap())
}
