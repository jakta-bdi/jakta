package io.github.anitvam.agents.bdi.reasoning

import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.events.Event

/** A selection function chose element of type T from a collection of them */
fun interface SelectionFunction<T> {
    /** Method that select an element of type [T] from a collection of them */
    fun select(collection: Iterable<T>): T
}
