package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface ActionRequest {
    // val arguments: List<Term> // TODO(handled by kotlin compiler, right?)
    val agentContext: ASAgentContext
    val agentName: String
    val agentID: AgentID
    val invocationTimestamp: Time?

    fun reply(substitution: Substitution = Substitution.empty(), effects: List<ActionSideEffect>): ActionResponse

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: ActionSideEffect): ActionResponse

    companion object {
        fun of(agent: ASAgent, requestTime: Time?) =
            of(agent.context.snapshot(), agent.name, agent.agentID, requestTime)

        fun of(
            agentContext: ASAgentContext,
            agentName: String,
            agentID: AgentID,
            invocationTimestamp: Time?
        ): ActionRequest = object : ActionRequest {
            override val agentContext: ASAgentContext = agentContext
            override val agentName: String = agentName
            override val agentID: AgentID = agentID
            override val invocationTimestamp: Time? = invocationTimestamp

            override fun reply(substitution: Substitution, effects: List<ActionSideEffect>): ActionResponse =
                ActionResponse(substitution, effects)

            override fun reply(substitution: Substitution, vararg effects: ActionSideEffect) =
                reply(substitution, effects.asList())
        }
    }
}
