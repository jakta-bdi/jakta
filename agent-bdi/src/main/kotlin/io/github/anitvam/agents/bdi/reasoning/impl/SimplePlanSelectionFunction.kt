package io.github.anitvam.agents.bdi.reasoning.impl

import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.reasoning.EventSelectionFunction
import io.github.anitvam.agents.bdi.reasoning.PlanSelectionFunction

/** Simple implementation of [EventSelectionFunction] that takes the first element of the queue */
class SimplePlanSelectionFunction(private val selectedEvent : Event) : PlanSelectionFunction {

    override fun select(collection: Iterable<Plan>): Plan = TODO()
}