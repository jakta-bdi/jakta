package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.responses.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface InternalRequest : ActionRequest<ASMutableAgentContext, AgentChange, InternalResponse> {
    companion object {
        fun of(agent: ASAgent, requestTime: Time?, arguments: Iterable<Term>): InternalRequest =
            object : InternalRequest {
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

                override fun reply(substitution: Substitution, effects: Iterable<AgentChange>) =
                    InternalResponse(substitution, effects)

                override fun reply(substitution: Substitution, vararg effects: AgentChange) =
                    reply(substitution, effects.asList())
            }

        fun of(agent: ASAgent, requestTime: Time?, vararg arguments: Term): InternalRequest =
            of(agent, requestTime, arguments.asList())
    }
}
