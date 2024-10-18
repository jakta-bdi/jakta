package it.unibo.jakta.events.impl

import it.unibo.jakta.events.PrologEvent
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.intentions.Intention

internal data class EventImpl<X>(
    override val trigger: Trigger<X>,
    override val intention: Intention?,
) : PrologEvent<X>
