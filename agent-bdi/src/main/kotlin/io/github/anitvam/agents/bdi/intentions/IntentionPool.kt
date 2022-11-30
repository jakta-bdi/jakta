package io.github.anitvam.agents.bdi.intentions

import io.github.anitvam.agents.bdi.intentions.impl.IntentionPoolImpl

interface IntentionPool : Map<IntentionID, Intention> {
    fun update(intention: Intention): IntentionPool

    fun nextIntention(): Intention

    fun pop(): IntentionPool

    companion object {
        fun empty(): IntentionPool = IntentionPoolImpl()

        fun of(intentions: Map<IntentionID, Intention>): IntentionPool = IntentionPoolImpl(intentions)
        fun of(vararg intentions: Intention): IntentionPool = of(intentions.asList())
        fun of(intentions: List<Intention>): IntentionPool = IntentionPoolImpl(intentions.associateBy { it.id })
    }
}
