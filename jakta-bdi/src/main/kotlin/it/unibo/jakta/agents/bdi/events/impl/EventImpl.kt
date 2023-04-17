package it.unibo.jakta.agents.bdi.events.impl

import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.events.Trigger
import it.unibo.jakta.agents.bdi.intentions.Intention

internal data class EventImpl(
    override val trigger: Trigger,
    override val intention: Intention?,
) : Event
