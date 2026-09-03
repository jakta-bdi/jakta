package it.unibo.jakta.dsl.agent

import it.unibo.jakta.dsl.JaktaDSL

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
