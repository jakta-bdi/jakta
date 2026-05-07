package it.unibo.jakta.reflection

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Testing it.unibo.jakta.reflection.Event.
 */
sealed interface Event {
    /**
     * Goal related to this event.
     * @param block
     * @return an [Event]
     */
    class Goal(val block: suspend () -> Unit) : Event

    /**
     * Continuation related to this event.
     * @param runnable [Runnable]
     * @return an [Event]
     */
    class Continuation(val runnable: Runnable) : Event
}

/**
 * Testing behavior of coroutine for Jakta new engine implementation.
 */
fun main(): Unit = runBlocking {
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
                    // THIS IS THE KEY -> The plans should not be children of the agent, but siblings of the agent
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

    // it.unibo.jakta.reflection.Event 1: long-running
    eventChannel.send(
        Event.Goal {
            println("it.unibo.jakta.reflection.Event 1 start")
            delay(1000)
            println("it.unibo.jakta.reflection.Event 1 end")
        },
    )

    eventChannel.send(
        Event.Goal {
            println("it.unibo.jakta.reflection.Event 2 start")
            delay(300)
            println("Cancelling scope")
            scope.cancel()
            println("it.unibo.jakta.reflection.Event 2 end")
        },
    )

    // Give enough time for events to process
    processor.join()
    println("Main done")
}
