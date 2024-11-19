package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.tuprolog.core.Substitution

data class ExternalResponse(
    override val substitution: Substitution,
    val effects: Iterable<EnvironmentChange>,
) : ActionResponse<EnvironmentChange>, List<EnvironmentChange> by effects.toList()
