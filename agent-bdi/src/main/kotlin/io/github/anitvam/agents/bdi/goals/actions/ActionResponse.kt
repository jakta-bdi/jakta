package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.goals.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.goals.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution

sealed interface ActionResponse<C : SideEffect> {
    val substitution: Substitution
    val effects: Iterable<C>
}

data class InternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<AgentChange>,
) : ActionResponse<AgentChange>

data class ExternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<EnvironmentChange>,
) : ActionResponse<EnvironmentChange>
