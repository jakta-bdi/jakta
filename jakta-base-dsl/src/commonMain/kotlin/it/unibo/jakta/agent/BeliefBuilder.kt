package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL

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

/**
 * Implementation of the BeliefBuilder interface.
 */
class BeliefBuilderImpl<Belief : Any>(private val addBelief: (Belief) -> Unit) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() {
        addBelief(this)
    }
}
