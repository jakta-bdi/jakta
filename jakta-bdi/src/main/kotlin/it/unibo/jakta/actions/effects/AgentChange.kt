package it.unibo.jakta.actions.effects

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan

interface AgentChange : ActionResult

interface BeliefChange: AgentChange {
    val belief: ASBelief

    class BeliefAddition(override val belief: ASBelief): BeliefChange
    class BeliefRemoval(override val belief: ASBelief): BeliefChange
}

interface IntentionChange : AgentChange {
    val intention: ASIntention

    class IntentionAddition(override val intention: ASIntention): IntentionChange
    class IntentionRemoval(override val intention: ASIntention): IntentionChange
}

interface EventChange : AgentChange {
    val event: ASEvent

    class EventAddition(override val event: ASEvent): EventChange
    class EventRemoval(override val event: ASEvent): EventChange
}

interface PlanChange : AgentChange {
    val plan: ASPlan

    class PlanAddition(override val plan: ASPlan) : PlanChange
    class PlanRemoval(override val plan: ASPlan) : PlanChange
}

class Sleep(val millis: Long) : AgentChange

object Stop : AgentChange

object Pause : AgentChange
