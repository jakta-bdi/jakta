package it.unibo.jakta.agent

import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventReceiver
import it.unibo.jakta.event.EventSource
import it.unibo.jakta.intention.IntentionPool
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType


//TODO the engine its what was previously the MAS
interface Engine {
    suspend fun runAgent(agent: RunnableAgent<*, *, *>)
}

//TODO either needs a reference to the Engine or notifies it some other way of agent creation



// Example for spatial environment

interface SpatialBody<P: Any> : AgentBody {
    val position: P
}

interface SpatialEnvironment<P: Any> : Environment<SpatialBody<P>>

interface MovementSkill<P: Any> {
    suspend fun moveTo(position: P)
}
