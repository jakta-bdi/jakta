package it.unibo.jakta.plans

import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.Intention

sealed interface ExecutionResult

interface SideEffects : ExecutionResult {
    val events: List<Event>
}

interface InternalEvent : ExecutionResult, Event {
    val intention: Intention
}

/**
 * Represents one of the steps that need to be executed for the [Plan]'s successful completion.
 */
interface Task<out Result : ExecutionResult> {
    suspend fun execute(): Result
}

interface Action : Task<SideEffects>

interface Achieve : Task<InternalEvent>

//class MyAction(context: AgentContext<* ,*,* >) : Action {
//
//    override suspend fun execute(): SideEffects {
//        println(context.addBelief)
//        return object: SideEffects {
//            override val events: List<Event> = listOf(AddBelief)
//        }
//    }
//}
//
//fun AgentContext<*, * , *>.myAction() = MyAction(this)
