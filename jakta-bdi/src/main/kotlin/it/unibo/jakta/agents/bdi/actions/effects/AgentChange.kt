package it.unibo.jakta.agents.bdi.actions.effects

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.context.ContextUpdate
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.jakta.agents.bdi.plans.Plan

sealed interface AgentChange : SideEffect

sealed interface InternalChange : AgentChange {
    val changeType: ContextUpdate
}

sealed interface ActivityChange : AgentChange

data class BeliefChange(
    val belief: Belief,
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
