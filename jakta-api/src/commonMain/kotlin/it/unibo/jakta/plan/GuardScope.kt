package it.unibo.jakta.plan

/**
 * Scope providing access to the beliefs relevant for evaluating a guard.
 */
data class GuardScope<Belief : Any, PlanContext : Any>(
    /**
     * The *beliefs* currently held by the agent.
     */
    val beliefs: Collection<Belief>,
    val context: PlanContext,
)
