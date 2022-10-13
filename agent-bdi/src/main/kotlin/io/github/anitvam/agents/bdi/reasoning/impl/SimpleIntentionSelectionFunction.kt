package io.github.anitvam.agents.bdi.reasoning.impl

import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.reasoning.IntentionSelectionFunction

class SimpleIntentionSelectionFunction : IntentionSelectionFunction{
    override fun select(collection: Iterable<Intention>): Intention = TODO()
}