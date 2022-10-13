package io.github.anitvam.agents.bdi.intentions

interface IntentionPool : Map<IntentionID, Intention> {
    fun update(intention: Intention): IntentionPool
}