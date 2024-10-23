package it.unibo.jakta.plans

import it.unibo.jakta.events.Event

/**
 * Represents one of the steps that need to be executed for the [Plan]'s successful completion.
 */
interface Task {

    /**
     * Represents the [Agent]'s activity of doing nothing.
     */
    data object Empty : Task

    /**
     * Execution of a generic activity during the execution of the [AgentLifecycle]'s iteration
     */
    interface ActionExecution<out Activity> : Task {
        val activity: Activity
    }

    /**
     * Requires the completion of an additional [Plan] for the completion of this [Task],
     * triggered by the specified [Event].
     */
    interface PlanExecution<out Trigger> : Task {
        val event: Event<Trigger>
    }
}
