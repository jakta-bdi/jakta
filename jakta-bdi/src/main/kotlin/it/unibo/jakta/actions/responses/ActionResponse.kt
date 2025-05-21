package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.SideEffect
import it.unibo.tuprolog.core.Substitution

interface ExecutionResult<out SideEffect> : List<SideEffect> {
    companion object {
        fun <SideEffect> from(events: List<SideEffect>): ExecutionResult<SideEffect> =
            object : ExecutionResult<SideEffect>, List<SideEffect> by events { }

        fun <SideEffect> from(vararg events: SideEffect): ExecutionResult<SideEffect> = from(events.toList())

        fun  <SideEffect> none(): ExecutionResult<SideEffect> = from(emptyList())
    }
}

class ActionResponse(
    val substitution: Substitution,
    effects: List<SideEffect>,
) : ExecutionResult<SideEffect>, List<SideEffect> by effects
