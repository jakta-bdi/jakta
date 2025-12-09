package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class PerceivingEnvironment<PerceptionPayload : Any, Belief : Any>(
    private val agents: Set<Agent<*, *, PerceivingEnvironment<PerceptionPayload, Belief>>>,
    val perceptionToBeliefMappingFunction: PerceptionPayload.() -> Belief,
) : Environment {

    private val externalEvents: Channel<Event.External> = Channel(Channel.UNLIMITED)
    private var job: Job? = null

    override fun enqueueExternalEvent(event: Event.External) {
        externalEvents.trySend(event) // Safe trySend() invocation since the Channel cannot overflow
    }

    override fun startPerceiving(scope: CoroutineScope) {
        job = scope.launch {
            while (true) {
                val event = externalEvents.receive()
                when (event) {
                    is Event.External.Message -> TODO()
                    is Event.External.Perception<*> -> {
                        agents.forEach {
                            it.trySend(
                                BeliefAddEvent(perceptionToBeliefMappingFunction(event.payload as PerceptionPayload)),
                            )
                        }
                    }

                    else -> error("Unrecognized event: $event")
                }
            }
        }
    }

    override fun stopPerceiving() {
        job?.cancel()
    }
}
