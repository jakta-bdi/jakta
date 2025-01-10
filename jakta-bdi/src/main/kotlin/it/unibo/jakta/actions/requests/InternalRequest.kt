package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.Action
import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

class InternalRequest (
    override val agentContext: ASAgentContext,
    override val agentName: String,
    override val agentID: AgentID,
    override val requestTimestamp: Time?,
//    override val arguments: List<Term>,
) : ActionRequest {

    constructor(agent: ASAgent, requestTime: Time?):
        this(agent.context.snapshot(), agent.name, agent.agentID, requestTime)

    override fun reply(substitution: Substitution, effects: List<ActionSideEffect>): ActionResponse =
        ActionResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: ActionSideEffect) =
        reply(substitution, effects.asList())
}
