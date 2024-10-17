package it.unibo.jakta.events.impl

import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.intentions.Intention

internal data class EventImpl(
    override val trigger: Trigger,
    override val intention: Intention?,
) : Event
