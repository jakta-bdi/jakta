package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.responses.ExternalResponse
import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface ExternalRequest : ActionRequest<Any, ActionResult<Any>, ExternalResponse> {
    val environment: Environment

    companion object {
        fun of(
            environment: Environment,
            agent: ASAgent,
            requestTime: Time?,
            arguments: Iterable<Term>,
        ): ExternalRequest = object: ExternalRequest {
            override val environment: Environment
                get() = environment
            override val arguments: List<Term>
                get() = arguments.toList()
            override val agentContext: ASAgentContext
                get() = agent.context.snapshot()
            override val agentName: String
                get() = agent.name
            override val agentID: AgentID
                get() = agent.agentID
            override val requestTimestamp: Time?
                get() = requestTime

            override fun reply(substitution: Substitution, effects: Iterable<ActionResult<Any>>): ExternalResponse =
                ExternalResponse(substitution, effects)

            override fun reply(substitution: Substitution, vararg effects: ActionResult<Any>): ExternalResponse =
                reply(substitution, effects.asList())
        }

        fun of(environment: Environment, agent: ASAgent, requestTime: Time?, vararg arguments: Term): ExternalRequest =
            of(environment, agent, requestTime, arguments.asList())
    }
}
