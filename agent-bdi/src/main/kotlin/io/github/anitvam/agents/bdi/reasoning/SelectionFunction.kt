package io.github.anitvam.agents.bdi.reasoning

import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.reasoning.impl.SimpleEventSelectionFunction
import io.github.anitvam.agents.bdi.reasoning.impl.SimpleIntentionSelectionFunction
import io.github.anitvam.agents.bdi.reasoning.impl.SimplePlanSelectionFunction

/** A selection function chose element of type T from a collection of them */
fun interface SelectionFunction<T> {
    /** Method that select an element of type [T] from a collection of them */
    fun select(collection: Iterable<T>): T
}

// interface MessageSelectionFunction: SelectionFunction<Message>

/** Selection Function of a [Plan] */
interface PlanSelectionFunction : SelectionFunction<Plan> {

    fun select(collection: Iterable<Plan>, selectedEvent: Event): Plan = select(collection)
    companion object {
        fun simpleOf(event: Event) = SimplePlanSelectionFunction(event)
    }
}

/** Selection Function of a [Event] */
interface EventSelectionFunction : SelectionFunction<Event> {
    companion object {
        fun simple() = SimpleEventSelectionFunction()
    }
}

/** Selection Function of a [Intention] */
interface IntentionSelectionFunction : SelectionFunction<Intention> {
    companion object {
        fun simple() = SimpleIntentionSelectionFunction()
    }
}