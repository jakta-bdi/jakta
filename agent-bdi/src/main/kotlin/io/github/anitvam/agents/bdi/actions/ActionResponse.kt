package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution

interface ActionResponse<C : SideEffect> {
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
