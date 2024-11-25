package it.unibo.jakta.actions.requests

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface ActionRequest<C : ActionResult, Res : ActionResponse<C>> {
    val arguments: List<Term>
    val agentContext: ASAgentContext
    val agentName: String
    val agentID: AgentID
    val requestTimestamp: Time?

    fun reply(substitution: Substitution = Substitution.empty(), effects: Iterable<ActionResult>): Res

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: ActionResult): Res
}
