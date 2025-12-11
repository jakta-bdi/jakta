package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.Movement.Events.position
import it.unibo.jakta.environment.Recharging.Events.chargeLevel
import it.unibo.jakta.event.Event
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay

/**
 * Environment which listens for external events and is capable to forward them to agents.
 * @param perceptionToBeliefMappingFunction mapping function which defines how to convert from a [PerceptionPayload] type to a [Belief] type the information.
 * @param agentFilteringFunction filtering function which potentially selects a subset of agents that will receive the information.
 */
class PerceivingEnvironment<PerceptionPayload : Any, Belief : Any, Goal : Any>(
    val perceptionToBeliefMappingFunction: Any.() -> Belief,
    val agentFilteringFunction: Agent<Belief, Goal, PerceivingEnvironment<PerceptionPayload, Belief, Goal>>.(
        Any,
    ) -> Boolean = { true }, // TODO(Maybe not enough)
) : Environment {

    private val perceptionBroker =
        PerceptionBroker<PerceptionPayload, Belief, Goal, PerceivingEnvironment<PerceptionPayload, Belief, Goal>>(
            perceptionToBeliefMappingFunction = perceptionToBeliefMappingFunction,
            agentFilteringFunction = agentFilteringFunction,
        )

    /**
     * Shares an [Event.External] to agents sharing this environment instance.
     * @param [Event.External] the event that will be possibly notified to agents.
     */
    fun enqueueExternalEvent(event: Event.External) {
        perceptionBroker.trySend(event) // Safe trySend() invocation since the Channel cannot overflow
    }

    /**
     * The environment listens for the next [Event.External] and informs agents about it.
     * @param agents the [Agent]s that will be informed.
     */
    suspend fun perceive(agents: Set<Agent<Belief, Goal, PerceivingEnvironment<PerceptionPayload, Belief, Goal>>>) {
        perceptionBroker.perceive(agents)
    }
}

//This will become the actual Environment interface
interface BrokerEnvironment : Environment {
    val broker : SendChannel<Event.External.Perception<out BrokerEnvironment, *>>
}

interface SpatialEnvironment<Position> : BrokerEnvironment {
    val position: Position
}

//concrete implementation of an environment
data class GridEnvironment(override val position: Pair<Int, Int>)
    : SpatialEnvironment<Pair<Int, Int>> {
        override val broker : SendChannel<Event.External.Perception<out BrokerEnvironment, *>> = TODO()
    }

//Interface for skills, skills depend on an environment to be able to send perceptions
//and possibly interact with environment-specific features
interface Skill<Env: Environment> {
    val environment: Env
}

//This is a skill that depends on a generic environment, i.e. it does not need any specific feature
//apart from the broker to notify the agent of charge complete
//Lets the agent recharge its battery
interface Recharging : Skill<BrokerEnvironment> {
    suspend fun recharge()

    //factory for events of Recharging
    //receiver prevents usage from outside Recharging extensions
    object Events {
        fun Recharging.chargeLevel(level: Int): ChargeLevel =
            ChargeLevel(level)
    }
}

data class ChargeLevel internal constructor(val level: Int) :
    Event.External.Perception<BrokerEnvironment, Recharging>

//Implementation of the actual skill for recharging
data class FixedTimeRecharging(override val environment: BrokerEnvironment) : Recharging {
    override suspend fun recharge() {
        println("Recharging...")
        delay(3000)
        println("Recharged!")
        environment.broker.trySend(this.chargeLevel(100))
    }
}

//This is a generic movement skill that depends on an environment with some spatial nature
interface Movement<P> : Skill<SpatialEnvironment<P>> {
    fun moveTo(direction: P) : P

    object Events {
        fun <P> Movement<P>.position(pos: P): Position<P> =
            Position(pos)
    }
}

data class Position<P> internal constructor (val position: P) :
    Event.External.Perception<SpatialEnvironment<P>, Movement<P>>


data class GridMovement(override val environment: GridEnvironment) : Movement<Pair<Int, Int>> {
    override fun moveTo(direction: Pair<Int, Int>) : Pair<Int, Int> {
        val (x, y) = environment.position
        val (dx, dy) = direction
        val newPos =  (x+dx to y+dy)
        environment.broker.trySend(this.position(newPos))
        return newPos

    }
}

val myEnv = GridEnvironment(0 to 0 )

object SkillSet :
    Recharging by FixedTimeRecharging(myEnv),
    Movement<Pair<Int, Int>> by GridMovement(myEnv) {
    override val environment: GridEnvironment = myEnv
}


