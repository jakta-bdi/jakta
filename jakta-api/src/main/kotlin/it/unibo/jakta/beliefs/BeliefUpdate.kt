package it.unibo.jakta.beliefs

import it.unibo.jakta.context.Addition
import it.unibo.jakta.context.ContextUpdate
import it.unibo.jakta.context.Removal

data class BeliefUpdate<B : Belief<*>>(
    val belief: B,
    val updateType: ContextUpdate,
) {
    companion object {
        fun <B : Belief<*>> removal(belief: B) = BeliefUpdate(belief, Removal)
        fun <B : Belief<*>> addition(belief: B) = BeliefUpdate(belief, Addition)
    }
}
