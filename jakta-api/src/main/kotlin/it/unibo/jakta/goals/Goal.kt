package it.unibo.jakta.goals

import it.unibo.jakta.beliefs.Belief

sealed interface Goal<X> {
    val value: X
}

data class EmptyGoal<X>(override val value: X) : Goal<X>

data class AddBelief<B : Belief<*>> (override val value: B) : Goal<B>

data class RemoveBelief<B : Belief<*>> (override val value: B) : Goal<B>

data class UpdateBelief<B : Belief<*>> (override val value: B) : Goal<B>

data class Achieve<X> (override val value: X) : Goal<X>

data class Test<X> (override val value: X) : Goal<X>

data class Spawn<X> (override val value: X) : Goal<X>

data class Act<X> (override val value: X) : Goal<X>

data class ActInternally<X> (override val value: X) : Goal<X>

data class ActExternally<X> (override val value: X) : Goal<X>
