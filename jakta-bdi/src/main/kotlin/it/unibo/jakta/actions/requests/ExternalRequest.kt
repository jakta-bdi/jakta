package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface ExternalRequest : ActionRequest {
    val environment: BasicEnvironment

    companion object {
        fun of(
            environment: BasicEnvironment,
            agent: ASAgent,
            requestTime: Time?,
            arguments: Iterable<Term>,
        ): ExternalRequest = object: ExternalRequest {
            override val environment: BasicEnvironment
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

            override fun reply(substitution: Substitution, effects: List<ActionSideEffect>): ActionResponse =
                ActionResponse(substitution, effects)

            override fun reply(substitution: Substitution, vararg effects: ActionSideEffect): ActionResponse =
                reply(substitution, effects.asList())
        }

        fun of(environment: BasicEnvironment, agent: ASAgent, requestTime: Time?, vararg arguments: Term): ExternalRequest =
            of(environment, agent, requestTime, arguments.asList())
    }
}
