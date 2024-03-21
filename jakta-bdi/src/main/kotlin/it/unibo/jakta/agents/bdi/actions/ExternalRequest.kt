package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.impl.ExternalRequestImpl
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.fsm.time.Time
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
