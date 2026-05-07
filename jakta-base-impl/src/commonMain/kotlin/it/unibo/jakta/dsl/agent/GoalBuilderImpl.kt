package it.unibo.jakta.dsl.agent

/**
 * Implementation of the GoalBuilder interface.
 */
class GoalBuilderImpl<Goal : Any>(private val addGoal: (Goal) -> Unit) : GoalBuilder<Goal> {
    override operator fun Goal.not() {
        addGoal(this)
    }
}
