package it.unibo.jakta.plans

import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.Intention
import javax.management.Query

interface ExecutionResult<out SideEffect> : List<SideEffect> {
    companion object {
        fun <SideEffect> from(events: List<SideEffect>): ExecutionResult<SideEffect> =
            object : ExecutionResult<SideEffect>, List<SideEffect> by events { }

        fun <SideEffect> from(vararg events: SideEffect): ExecutionResult<SideEffect> = from(events.toList())

        fun  <SideEffect> none(): ExecutionResult<SideEffect> = from(emptyList())
    }
}

//interface InternalEvent<Query: Any, Belief, Event> : // TODO("I just don't like the name of this")
//    ExecutionResult,
//    Intention<Query, Belief, Event, ActivationRecord<Query, Belief, Event>>

///**
// * Represents one of the steps that need to be executed for the [Plan]'s successful completion.
// */
//interface Task<Query: Any, Belief, in Argument, out Result : ExecutionResult> {
//    suspend fun execute(argument: Argument): Result
//}


//interface Achieve<Query: Any, Belief, in Argument, Event> :
//    Task<Query, Belief, Argument,
//    InternalEvent<Query, Belief, Event>>

