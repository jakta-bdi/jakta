package io.github.anitvam.agents.bdi.actions.impl

import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.actions.ExternalResponse
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

class ExternalRequestImpl(
    override val environment: Environment,
    override val arguments: List<Term>,
    override val sender: String,
    override val requestTimestamp: Time = Time.of(System.currentTimeMillis()),
) : ExternalRequest {
    override fun reply(substitution: Substitution, effects: Iterable<EnvironmentChange>) =
        ExternalResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: EnvironmentChange) =
        reply(substitution, effects.asList())
}
