package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan

interface InternalAction : Action<AgentChange, InternalResponse, InternalRequest> {
    fun addBelief(belief: Belief)
    fun removeBelief(belief: Belief)
    fun addIntention(intention: Intention)
    fun removeIntention(intention: Intention)
    fun addEvent(event: Event)
    fun removeEvent(event: Event)
    fun addPlan(plan: Plan)
    fun removePlan(plan: Plan)
    fun stopAgent()
    fun sleepAgent(millis: Long)
    fun pauseAgent()
}
