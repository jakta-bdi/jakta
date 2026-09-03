package it.unibo.jakta.belief

/**
 * Represents the belief base of an agent.
 * It is a mutable collection of beliefs, that can notify the agent of changes via events.
 */
interface BeliefBase<Belief : Any> : MutableCollection<Belief> {
    /**
     * Returns a snapshot of the current beliefs in the belief base.
     */
    fun snapshot(): Collection<Belief>
}
