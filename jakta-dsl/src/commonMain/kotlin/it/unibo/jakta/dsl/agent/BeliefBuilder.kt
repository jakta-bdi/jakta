package it.unibo.jakta.dsl.agent

import it.unibo.jakta.dsl.JaktaDSL

/**
 * Builder interface for defining beliefs.
 */
@JaktaDSL
interface BeliefBuilder<Belief : Any> {
    /**
     * Adds the belief to the builder's collection of beliefs using the "+" operator.
     */
    operator fun Belief.unaryPlus()
}
