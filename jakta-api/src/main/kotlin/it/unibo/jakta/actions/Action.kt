package it.unibo.jakta.actions

interface SideEffect

fun interface Action<Belief : Any, Query : Any, Result> : (ActionInvocationContext<Belief, Query, Result>) -> List<SideEffect> {

    interface WithoutSideEffect<Belief : Any, Query : Any, Result> : Action<Belief, Query, Result> {

        override fun invoke(context: ActionInvocationContext<Belief, Query, Result>): List<SideEffect> =
            emptyList<SideEffect>().also { execute(context) }

        fun execute(context: ActionInvocationContext<Belief, Query, Result>)
    }
}
