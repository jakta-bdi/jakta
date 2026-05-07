package it.unibo.jakta

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.*

sealed interface Event {
    class Goal(val block: suspend () -> Unit) : Event
    class Continuation(val runnable: Runnable) : Event
}

fun main() = runBlocking {
    // Unlimited channel for events
    val eventChannel = Channel<Event>(Channel.UNLIMITED)

    // Supervisor for all event coroutines
    val supervisor = SupervisorJob()
    val scope = CoroutineScope(coroutineContext + supervisor)

    // Custom dispatcher that re-enqueues continuations back into the channel
    val dispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            val job = context[Job]
            if (job?.isActive == true) {
                eventChannel.trySend(Event.Continuation(block))
            }
        }
    }

    // Processor coroutine: stops automatically when the scope is cancelled
    val processor = scope.launch {
        while (isActive) {
            when (val event = eventChannel.receive()) {

                is Event.Goal -> {
                    // Start a new coroutine
                    //THIS IS THE KEY -> The plans should not be children of the agent, but siblings of the agent
                    scope.launch(dispatcher) {
                            event.block()
                    }
                }

                is Event.Continuation -> {
                    // Actually execute continuation
                    event.runnable.run()
                }
            }
        }
    }

    // Event 1: long-running
    eventChannel.send(Event.Goal {
        println("Event 1 start")
        delay(1000)
        println("Event 1 end")
    })

    eventChannel.send(Event.Goal {
        println("Event 2 start")
        delay(300)
        println("Cancelling scope")
        scope.cancel()
        println("Event 2 end")
    })

    // Give enough time for events to process
    processor.join()
    println("Main done")
}
