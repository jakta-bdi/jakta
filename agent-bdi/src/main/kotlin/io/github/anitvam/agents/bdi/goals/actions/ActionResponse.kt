package io.github.anitvam.agents.bdi.goals.actions

import it.unibo.tuprolog.core.Substitution

sealed interface ActionResponse<Change : SideEffect> {
    val substitution: Substitution
    val effects: Iterable<Change>
}

data class InternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<AgentChange>,
) : ActionResponse<AgentChange>

data class ExternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<EnvironmentChange>,
) : ActionResponse<EnvironmentChange>
