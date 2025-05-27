package it.unibo.jakta.actions

interface SideEffect

fun interface Action : (ActionInvocationContext) -> List<SideEffect> {
    interface WithoutSideEffect : Action {

        override fun invoke(context: ActionInvocationContext): List<SideEffect> =
            emptyList<SideEffect>().also { execute(context) }

        fun execute(context: ActionInvocationContext)
    }
}
