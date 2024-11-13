package it.unibo.jakta.plans

import it.unibo.jakta.events.Event
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

interface InternalEvent<Query: Any, Belief> : ExecutionResult, Event, Intention<Query, Belief> // TODO("I just don't like the name of this")

/**
 * Represents one of the steps that need to be executed for the [Plan]'s successful completion.
 */
interface Task<Query: Any, Belief, in Argument, out Result : ExecutionResult> {
    suspend fun execute(intention: Intention<Query, Belief>, argument: Argument): Result // QUESTA MI DA' FASTIDIO, quando creo un task sto dicendo che voglio fare una certa cosa, non che cosa viene eseguito sotto.
}

interface Action<Query: Any, Belief, in Argument, Event> : Task<Query, Belief, Argument, ActionTaskEffects<Event>>

interface Achieve<Query: Any, Belief, in Argument> : Task<Query, Belief, Argument, InternalEvent<Query, Belief>>

// class MyAction(context: AgentContext<* ,*,* >) : Action {
//
//    override suspend fun execute(): ActionTaskEffects {
//        println(context.addBelief)
//        return object: ActionTaskEffects {
//            override val events: List<Event> = listOf(AddBelief)
//        }
//    }
// }
//
// fun AgentContext<*, * , *>.myAction() = MyAction(this)
