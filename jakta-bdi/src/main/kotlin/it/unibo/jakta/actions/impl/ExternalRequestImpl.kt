package it.unibo.jakta.actions.impl

import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.ExternalResponse
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.fsm.time.Time
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
