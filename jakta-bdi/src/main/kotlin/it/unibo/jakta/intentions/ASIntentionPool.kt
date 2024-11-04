package it.unibo.jakta.intentions

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.impl.ASIntentionPoolImpl
import it.unibo.tuprolog.core.Struct

interface ASIntentionPool : IntentionPool<Struct, ASBelief> {

    companion object {
        fun empty(): ASIntentionPool = ASIntentionPoolImpl()

        fun of(intentions: Map<IntentionID, ASIntention>): ASIntentionPool = ASIntentionPoolImpl(intentions)
        fun of(vararg intentions: ASIntention): ASIntentionPool = of(intentions.asList())
        fun of(intentions: List<ASIntention>): ASIntentionPool = ASIntentionPoolImpl(intentions.associateBy { it.id })
    }
}
