package it.unibo.jakta.actions.effects

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.context.ContextUpdate
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.Plan

sealed interface AgentChange : ActionResult

sealed interface InternalChange : AgentChange {
    val changeType: ContextUpdate
}

sealed interface ActivityChange : AgentChange

data class BeliefChange(
    val belief: ASBelief,
    override val changeType: ContextUpdate,
) : InternalChange

data class IntentionChange(
    val intention: Intention,
    override val changeType: ContextUpdate,
) : InternalChange

data class EventChange(
    val event: Event,
    override val changeType: ContextUpdate,
) : InternalChange

data class PlanChange(
    val plan: Plan,
    override val changeType: ContextUpdate,
) : InternalChange

data class Sleep(val millis: Long) : ActivityChange

class Stop : ActivityChange

class Pause : ActivityChange
