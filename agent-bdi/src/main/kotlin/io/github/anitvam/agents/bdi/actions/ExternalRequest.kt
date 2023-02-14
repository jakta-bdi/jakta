package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.impl.ExternalRequestImpl
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.fsm.time.Time
import it.unibo.tuprolog.core.Term

interface ExternalRequest : ActionRequest<EnvironmentChange, ExternalResponse> {
    val environment: Environment
    val sender: String

    companion object {
        fun of(
            environment: Environment,
            sender: String,
            requestTime: Time,
            arguments: Iterable<Term>
        ): ExternalRequest = ExternalRequestImpl(environment, sender, requestTime, arguments.toList())

        fun of(environment: Environment, sender: String, requestTime: Time, vararg arguments: Term): ExternalRequest =
            ExternalRequestImpl(environment, sender, requestTime, arguments.asList())
    }
}
