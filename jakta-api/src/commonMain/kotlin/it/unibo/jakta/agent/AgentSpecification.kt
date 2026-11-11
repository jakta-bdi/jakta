package it.unibo.jakta.agent

import it.unibo.jakta.plan.PlanSelection

/**
 * The specification of an [Agent],
 * which includes its [body], [initialState], [initialGoals], and [id].
 */
interface AgentSpecification<Belief : Any, Goal : Any, Body : Any> {
    val id: AgentID
    val body: Body
    val initialGoals: List<Goal>
    val initialState: AgentState<Belief, Goal>

    /**
     * How the agent chooses among the applicable plans for an event: by default, the first one.
     */
    val planSelection: PlanSelection<Belief, Goal>
        get() = PlanSelection.first()

    /**
     * How the agent reacts when an intention fails with nobody left to handle the failure,
     * or null to log a warning: by default, null.
     */
    val failedIntentionHandler: FailedIntentionHandler<Belief, Goal>?
        get() = null
}
