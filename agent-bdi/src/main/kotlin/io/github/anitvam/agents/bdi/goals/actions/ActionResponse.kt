package io.github.anitvam.agents.bdi.goals.actions

import it.unibo.tuprolog.core.Substitution

sealed interface ActionResponse<Change : SideEffect> {
    val substitution: Substitution
    val effects: List<Change>
}

sealed interface InternalResponse : ActionResponse<AgentChange>

sealed interface ExternalResponse : ActionResponse<EnvironmentChange>
