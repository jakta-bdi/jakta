package it.unibo.jakta.agents.bdi.actions.impl

import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.ExternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

class ExternalRequestImpl(
    override val environment: Environment,
    override val sender: String,
    override val requestTimestamp: Time?,
    override val arguments: List<Term>,
) : ExternalRequest {
    override fun reply(substitution: Substitution, effects: Iterable<EnvironmentChange>) =
        ExternalResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: EnvironmentChange) =
        reply(substitution, effects.asList())
}
