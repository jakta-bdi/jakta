package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.SideEffect

fun interface Action<Belief : Any, Query : Any, Response> :
    suspend (ActionInvocationContext<Belief, Query, Response>) -> List<SideEffect<Belief, Query, Response>> {
    interface WithoutSideEffect<Belief : Any, Query : Any, Response> : Action<Belief, Query, Response> {
        override suspend fun invoke(context: ActionInvocationContext<Belief, Query, Response>): List<SideEffect<Belief, Query, Response>> =
            emptyList<SideEffect<Belief, Query, Response>>().also { execute(context) }

        suspend fun execute(context: ActionInvocationContext<Belief, Query, Response>)
    }
}
