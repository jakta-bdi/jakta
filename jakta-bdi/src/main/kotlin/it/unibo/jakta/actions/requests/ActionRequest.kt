package it.unibo.jakta.actions.requests

import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface ActionRequest {
    // val arguments: List<Term> // TODO(potrebbero non sevirmi più, ci pensa il compilatore kotlin)
    val agentContext: ASAgentContext
    val agentName: String
    val agentID: AgentID
    val requestTimestamp: Time?

    fun reply(substitution: Substitution = Substitution.empty(), effects: List<ActionSideEffect>): ActionResponse

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: ActionSideEffect): ActionResponse
}
