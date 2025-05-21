package it.unibo.jakta.plans

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.Action
import it.unibo.jakta.events.Event

interface Plan<Query : Any> {
    val id: PlanID
        get() = PlanID()
    val agent: Agent<Query>
    fun apply(event: Event): List<Action>
}
