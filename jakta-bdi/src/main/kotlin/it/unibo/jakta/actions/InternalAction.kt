package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.Plan

interface InternalAction : ASAction<AgentChange, InternalResponse, InternalRequest> {
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
