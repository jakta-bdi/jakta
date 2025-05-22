package it.unibo.jakta.plans

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.Action
import it.unibo.jakta.events.Event

interface Plan {
    val id: PlanID
        get() = PlanID()
    fun apply(event: Event): List<Action>
}
