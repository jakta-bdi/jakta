package io.github.anitvam.agents.bdi.events.impl

import io.github.anitvam.agents.bdi.IntentionID
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.Trigger

class EventImpl(
    override val trigger: Trigger,
    override val intention: IntentionID?,
) : Event