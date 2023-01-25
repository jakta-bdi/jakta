package io.github.anitvam.agents.bdi.actions.effects

import io.github.anitvam.agents.bdi.context.ContextUpdate
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan

sealed interface AgentChange : SideEffect {
    val changeType: ContextUpdate
}

data class BeliefChange(
    val belief: Belief,
    override val changeType: ContextUpdate,
) : AgentChange

data class IntentionChange(
    val intention: Intention,
    override val changeType: ContextUpdate,
) : AgentChange

data class EventChange(
    val event: Event,
    override val changeType: ContextUpdate,
) : AgentChange

data class PlanChange(
    val plan: Plan,
    override val changeType: ContextUpdate,
) : AgentChange
