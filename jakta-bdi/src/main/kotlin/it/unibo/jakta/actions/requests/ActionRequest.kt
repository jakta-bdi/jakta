package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.Agent
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution

interface ActionRequest : ActionInvocationContext {

    override val agentContext: ASAgent.ASAgentContext

    fun reply(substitution: Substitution = Substitution.empty(), effects: List<SideEffect>): ActionResponse
    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: SideEffect): ActionResponse

    companion object {
        fun of(
            agent: ASAgent.ASAgentContext,
            invocationTimestamp: Time?,
        ): ActionRequest = object : ActionRequest {
            override val agentContext: ASAgent.ASAgentContext = agent
            override val invocationTimestamp: Time? = invocationTimestamp

            override fun reply(substitution: Substitution, effects: List<SideEffect>): ActionResponse =
                ActionResponse(substitution, effects)

            override fun reply(substitution: Substitution, vararg effects: SideEffect) =
                reply(substitution, effects.asList())
        }
    }
}
