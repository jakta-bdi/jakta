package it.unibo.jakta.plan

/**
 * Scope providing access to the beliefs relevant for evaluating a guard.
 */
interface GuardScope<Belief : Any> {
    /**
     * The *beliefs* currently held by the agent.
     */
    val beliefs: Collection<Belief>
}
