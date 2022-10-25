package io.github.anitvam.agents.bdi.intentions

import io.github.anitvam.agents.bdi.intentions.impl.IntentionPoolImpl

interface IntentionPool : Map<IntentionID, Intention> {
    fun update(intention: Intention): IntentionPool

    fun nextIntention(): Intention

    fun pop(): IntentionPool

    companion object {
        fun empty() : IntentionPool = IntentionPoolImpl()
    }
}