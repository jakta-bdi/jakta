package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.impl.ExternalRequestImpl
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Term

interface ExternalRequest : ActionRequest<EnvironmentChange, ExternalResponse> {
    val environment: Environment
    val sender: String

    companion object {
        fun of(
            environment: Environment,
            sender: String,
            requestTime: Time?,
            arguments: Iterable<Term>,
        ): ExternalRequest = ExternalRequestImpl(environment, sender, requestTime, arguments.toList())

        fun of(environment: Environment, sender: String, requestTime: Time?, vararg arguments: Term): ExternalRequest =
            ExternalRequestImpl(environment, sender, requestTime, arguments.asList())
    }
}
