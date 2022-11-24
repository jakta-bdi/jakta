package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.AgentContext
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

sealed interface ActionRequest<Change : SideEffect, Response : ActionResponse<Change>> {
    val arguments: List<Term>
    val data: Map<String, Any>

    fun reply(substitution: Substitution = Substitution.empty(), effects: Iterable<Change>): Response

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: Change): Response
}

sealed interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    val agent: AgentContext
}

sealed interface ExternalRequest : ActionRequest<EnvironmentChange, ExternalResponse>  {
    // add fields to let the action observe the environment
}
