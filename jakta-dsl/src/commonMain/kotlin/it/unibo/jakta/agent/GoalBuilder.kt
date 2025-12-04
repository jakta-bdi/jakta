package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL

/**
 * Builder interface for defining goals.
 */
@JaktaDSL
interface GoalBuilder<Goal : Any> {
    /**
     * Adds the goal to the builder's collection of goals using the "!" operator.
     */
    operator fun Goal.not()
}

/**
 * Implementation of the GoalBuilder interface.
 */
class GoalBuilderImpl<Goal : Any>(private val addGoal: (Goal) -> Unit) : GoalBuilder<Goal> {
    override operator fun Goal.not() {
        addGoal(this)
    }
}
