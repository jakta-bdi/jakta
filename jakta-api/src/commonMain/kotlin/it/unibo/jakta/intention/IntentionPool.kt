package it.unibo.jakta.intention


/**
 * Represents a pool of intentions managed by an agent.
 */
interface IntentionPool {

    /**
     * Returns the set of intentions currently in the pool.
     */
    fun getIntentionsSet(): Set<Intention>
}
