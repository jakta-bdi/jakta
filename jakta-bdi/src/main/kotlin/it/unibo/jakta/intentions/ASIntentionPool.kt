package it.unibo.jakta.intentions

import it.unibo.jakta.intentions.impl.ASIntentionPoolImpl

object IntentionPoolStaticFactory {
    fun empty(): ASMutableIntentionPool = ASIntentionPoolImpl()

    fun of(intentions: Map<IntentionID, ASIntention>): ASMutableIntentionPool =
        ASIntentionPoolImpl(intentions.toMutableMap())

    fun of(vararg intentions: ASIntention): ASMutableIntentionPool = of(intentions.asList())

    fun of(intentions: List<ASIntention>): ASMutableIntentionPool =
        ASIntentionPoolImpl(intentions.associateBy { it.id }.toMutableMap())
}
