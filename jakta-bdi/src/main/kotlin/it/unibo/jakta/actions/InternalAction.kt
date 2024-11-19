package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan

interface InternalAction : ASAction<AgentChange, InternalResponse, InternalRequest> {
    fun addBelief(belief: ASBelief)
    fun removeBelief(belief: ASBelief)
    fun addIntention(intention: ASIntention)
    fun removeIntention(intention: ASIntention)
    fun addEvent(event: ASEvent)
    fun removeEvent(event: ASEvent)
    fun addPlan(plan: ASPlan)
    fun removePlan(plan: ASPlan)
    fun stopAgent()
    fun sleepAgent(millis: Long)
    fun pauseAgent()
}
