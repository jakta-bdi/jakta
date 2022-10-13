package io.github.anitvam.agents.bdi.reasoning.impl

import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.reasoning.EventSelectionFunction

/** Simple implementation of [EventSelectionFunction] that takes the first element of the queue */
class SimpleEventSelectionFunction : EventSelectionFunction {
    override fun select(collection: Iterable<Event>): Event = collection.last()
}