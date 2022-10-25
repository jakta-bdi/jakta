package io.github.anitvam.agents.bdi.events.impl

import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.intentions.Intention

internal class EventImpl(
    override val trigger: Trigger,
    override val intention: Intention?,
) : Event