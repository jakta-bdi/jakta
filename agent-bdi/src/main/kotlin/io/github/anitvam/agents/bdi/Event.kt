package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.IntentionID
import io.github.anitvam.agents.bdi.Trigger

interface Event {
    val trigger: Trigger
    val intention: IntentionID?
}
