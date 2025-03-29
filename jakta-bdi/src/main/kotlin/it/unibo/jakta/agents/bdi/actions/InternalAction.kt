package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.jakta.agents.bdi.plans.Plan

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
