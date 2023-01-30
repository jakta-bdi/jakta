package io.github.anitvam.agents.bdi.actions.effects

import io.github.anitvam.agents.bdi.context.ContextUpdate
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan

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
