package it.unibo.jakta.dsl.agent

/**
 * Implementation of the BeliefBuilder interface.
 */
class BeliefBuilderImpl<Belief : Any>(private val addBelief: (Belief) -> Unit) : BeliefBuilder<Belief> {
    override operator fun Belief.unaryPlus() {
        addBelief(this)
    }
}
