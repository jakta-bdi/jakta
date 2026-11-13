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

    /**
     * Makes the beliefs in [scope] become [replacements], through the belief revision.
     * @param scope for each belief held, the belief to remove from it, or `null` if it is out of scope
     * (see [BeliefRevision.replace]).
     * @return whether the belief base changed.
     * @throws IllegalArgumentException if any of the [replacements] is not in [scope].
     */
    fun replace(replacements: List<Belief>, scope: (Belief) -> Belief?): Boolean
}
