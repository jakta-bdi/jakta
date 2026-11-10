package it.unibo.jakta.plan

import it.unibo.jakta.event.AgentEvent

/**
 * The *option selection function* of an agent (`S_O` in AgentSpeak, `selectOption` in Jason):
 * among the applicable plans for an event, it chooses the one to run.
 *
 * The default, [first], runs the first applicable plan in the order of the plan library.
 */
fun interface PlanSelection<Belief : Any, Goal : Any> {
    /**
     * Chooses the option to run for [event].
     *
     * @param event the event being handled: the addition or removal of a belief or goal, or a goal failure.
     * @param options the applicable plans, each with the context computed by its trigger and guard,
     * in the order of the plan library. They are computed lazily, so a guard is only evaluated if the sequence
     * gets that far: the sequence can be iterated only once (use `toList()` to look at it several times).
     * @return the option to run, or null to run none, as if no plan were applicable.
     */
    fun select(
        event: AgentEvent.Internal,
        options: Sequence<Option<Belief, Goal, *, *, *>>,
    ): Option<Belief, Goal, *, *, *>?

    /** Predefined plan selection functions. */
    companion object {
        /**
         * Selects the first applicable plan, in the order of the plan library: the default.
         */
        fun <Belief : Any, Goal : Any> first(): PlanSelection<Belief, Goal> =
            PlanSelection { _, options -> options.firstOrNull() }
    }
}
