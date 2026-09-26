package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.intention.Intention
import kotlinx.coroutines.Job

/**
 * A goal the agent is currently pursuing, i.e. a goal for which a plan is running.
 * @property event the goal event whose plan is pursuing the goal.
 * @property intention the intention in which the plan is running.
 * @property job the job of the running plan.
 */
data class Desire<Goal : Any>(val event: AgentEvent.Internal.Goal<Goal, *>, val intention: Intention, val job: Job) {
    /**
     * The goal being pursued.
     */
    val goal: Goal
        get() = event.goal
}

/**
 * Thrown to the plan waiting on a subgoal when that subgoal is dropped.
 * @property goal the dropped goal.
 */
class GoalDroppedException(val goal: Any) : Exception("Goal $goal has been dropped")
