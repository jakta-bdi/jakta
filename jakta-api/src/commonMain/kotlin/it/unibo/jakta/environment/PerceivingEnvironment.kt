package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.event.Event
import kotlinx.coroutines.channels.SendChannel

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


val broker : SendChannel<Event.External> = TODO()

interface SpatialEnvironment<Position> : Environment {
    val position: Position
}

data class GridEnvironment(override val position: Pair<Int, Int>)
    : SpatialEnvironment<Pair<Int, Int>> {}

interface Skill<Env: Environment> {
    val environment: Env
}

interface Cleaning : Skill<Environment> {
    fun clean()
}

interface Movement<P> : Skill<SpatialEnvironment<P>> {
    fun moveTo(direction: P) : P
}


data class CleaningImpl(override val environment: Environment) : Cleaning {
    override fun clean() {
        TODO("Not yet implemented")
    }

}


data class GridMovement(override val environment: GridEnvironment) : Movement<Pair<Int, Int>> {
    override fun moveTo(direction: Pair<Int, Int>) : Pair<Int, Int> {
        val (x, y) = environment.position
        val (dx, dy) = direction
        val newPos =  (x+dx to y+dy)
        broker.trySend(Position( newPos))
        return newPos
    }
}

data class Position<P>(val position: P) :
    Event.External.Perception<SpatialEnvironment<P>, Movement<P>>


val myEnv = GridEnvironment(0 to 0 )

//TODO OPS SI SPACCA QUI, ma c'ero quasi
//object SkillSet :
//    Cleaning by CleaningImpl(myEnv),
//    Movement<Pair<Int, Int>> by GridMovement(myEnv)

//SUBOTTIMO
object MySkills : Cleaning, Movement<Pair<Int, Int>> {

    override fun clean() {
        CleaningImpl(myEnv).clean()
    }

    override fun moveTo(direction: Pair<Int, Int>): Pair<Int, Int> {
        GridMovement(myEnv).moveTo(direction)
    }

    override val environment: SpatialEnvironment<Pair<Int, Int>>
        get() = myEnv
}
