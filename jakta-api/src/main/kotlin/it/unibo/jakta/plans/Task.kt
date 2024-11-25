package it.unibo.jakta.plans

import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.Intention
import javax.management.Query

sealed interface ExecutionResult

interface ActionTaskEffects<Event> : ExecutionResult, List<Event> {
    companion object {
        fun <Event> from(events: List<Event>): ActionTaskEffects<Event> =
            object : ActionTaskEffects<Event>, List<Event> by events { }

        fun <Event> from(vararg events: Event): ActionTaskEffects<Event> = from(events.toList())

        fun  <Event> none(): ActionTaskEffects<Event> = from(emptyList())
    }
}

//interface InternalEvent<Query: Any, Belief, Event> : // TODO("I just don't like the name of this")
//    ExecutionResult,
//    Intention<Query, Belief, Event, ActivationRecord<Query, Belief, Event>>

/**
 * Represents one of the steps that need to be executed for the [Plan]'s successful completion.
 */
interface Task<Query: Any, Belief, in Argument, out Result : ExecutionResult> {
    suspend fun execute(argument: Argument): Result
}


//interface Achieve<Query: Any, Belief, in Argument, Event> :
//    Task<Query, Belief, Argument,
//    InternalEvent<Query, Belief, Event>>

