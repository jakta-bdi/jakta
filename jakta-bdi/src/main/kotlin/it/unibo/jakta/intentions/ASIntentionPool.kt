package it.unibo.jakta.intentions

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.impl.IntentionPoolImpl
import it.unibo.tuprolog.core.Struct

interface ASIntentionPool : IntentionPool<Struct, ASBelief>

interface ASMutableIntentionPool {

    companion object {
        fun empty(): ASIntentionPool = IntentionPoolImpl()

        fun of(intentions: Map<IntentionID, Intention<Query, Belief>>): IntentionPool = IntentionPoolImpl(intentions)
        fun of(vararg intentions: Intention): IntentionPool = of(intentions.asList())
        fun of(intentions: List<Intention>): IntentionPool = IntentionPoolImpl(intentions.associateBy { it.id })
    }
}
