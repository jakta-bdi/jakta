package it.unibo.jakta.plan

/**
 * Scope providing access to the beliefs relevant for evaluating a guard.
 */
data class GuardScope<Belief : Any, PlanContext : Any>(
    /**
     * The *beliefs* currently held by the agent.
     */
    val beliefs: Collection<Belief>,
    /**
     * The *context* of the plan for which the guard is being evaluated,
     * containing information about the matched trigger.
     */
    val context: PlanContext,
)
