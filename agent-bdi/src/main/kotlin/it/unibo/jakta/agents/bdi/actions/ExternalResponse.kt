package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.tuprolog.core.Substitution

data class ExternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<EnvironmentChange>,
) : ActionResponse<EnvironmentChange>
