package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.actions.impl.InternalRequestImpl
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

sealed interface ActionRequest<C : SideEffect, Res : ActionResponse<C>> {
    val arguments: List<Term>
    // val data: Map<String, Any>

    fun reply(substitution: Substitution = Substitution.empty(), effects: Iterable<C>): Res

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: C): Res
}

interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    // val agent: AgentContext

    companion object {
        fun of(arguments: Iterable<Term>): InternalRequest = InternalRequestImpl(arguments.toList())

        fun of(vararg arguments: Term): InternalRequest = of(arguments.asList())
    }
}

interface ExternalRequest : ActionRequest<EnvironmentChange, ExternalResponse> {
    // add fields to let the action observe the environment
}
