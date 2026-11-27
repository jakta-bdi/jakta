package it.unibo.jakta.plan

import it.unibo.jakta.agent.MutableAgentState

/**
 * An *option*: an applicable [plan] together with the [context] that its trigger and guard produced
 * when it was selected, as the plan plus unifier of AgentSpeak.
 *
 * The agent runs the option with this [context]: the trigger and the guard are not evaluated again
 * when the plan starts, so the plan runs with the bindings it was selected with,
 * even if the beliefs changed in the meantime.
 */
data class Option<Belief : Any, Goal : Any, TriggerEntity : Any, Context : Any, PlanResult>(
    /**
     * The selected plan.
     */
    val plan: Plan<Belief, Goal, TriggerEntity, Context, PlanResult>,
    /**
     * The context returned by the guard of [plan] when it was selected.
     */
    val context: Context,
) {
    /**
     * Runs [plan] on the given [agent] with the [context] computed at selection time.
     */
    suspend fun run(agent: MutableAgentState<Belief, Goal>): PlanResult = plan.run(agent, context)
}
