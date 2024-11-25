package it.unibo.jakta.intentions

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.impl.ASIntentionPoolImpl
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct

typealias ASIntentionPool = IntentionPool<Struct, ASBelief, ASEvent, ASActivationRecord, ASIntention, ASPlan>

typealias ASMutableIntentionPool =
    MutableIntentionPool<Struct, ASBelief, ASEvent, ASActivationRecord, ASIntention, ASPlan>

object IntentionPoolStaticFactory{
    fun empty(): ASMutableIntentionPool = ASIntentionPoolImpl()

    fun of(intentions: Map<IntentionID, ASIntention>): ASMutableIntentionPool =
        ASIntentionPoolImpl(intentions.toMutableMap())

    fun of(vararg intentions: ASIntention): ASMutableIntentionPool = of(intentions.asList())

    fun of(intentions: List<ASIntention>): ASMutableIntentionPool =
        ASIntentionPoolImpl(intentions.associateBy { it.id }.toMutableMap())
}
