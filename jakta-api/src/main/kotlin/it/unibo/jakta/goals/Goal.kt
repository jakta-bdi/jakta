package it.unibo.jakta.goals

interface Goal {

    data object EmptyGoal : Goal

    data class Achieve<X>(override val value: X) : Goal<X>

    data class Test<X>(override val value: X) : Goal<X>

    data class Spawn<X>(override val value: X) : Goal<X>
}

interface NonEmptyGoal<out State> : Goal {
    val value: State
}

/**
 * [ExecutableGoal]s represent something which Agent executes in the current iteration of its lifecycle.
 */
interface ExecutableGoal<out State> : NonEmptyGoal<State> {
    data class AddBelief<out Belief>(override val value: B) : ExecutableGoal<B>

    data class RemoveBelief<B : Belief<*>>(override val value: B) : Goal<B>

    data class UpdateBelief<B : Belief<*>>(override val value: B) : Goal<B>

    data class Act<X>(override val value: X) : Goal<X>

    data class ActInternally<X>(override val value: X) : Goal<X>

    data class ActExternally<X>(override val value: X) : Goal<X>
}
