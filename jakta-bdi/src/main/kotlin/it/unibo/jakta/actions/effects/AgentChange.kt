package it.unibo.jakta.actions.effects

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan

sealed interface AgentChange : ActionResult

data class BeliefChange(
    val belief: ASBelief,
) : AgentChange

data class IntentionChange(
    val intention: ASIntention,
) : AgentChange

data class EventChange(
    val event: ASEvent,
) : AgentChange

data class PlanChange(
    val plan: ASPlan,
) : AgentChange

data class Sleep(val millis: Long) : AgentChange

data object Stop : AgentChange

data object Pause : AgentChange
