package it.unibo.pluto.jakta.actions

interface SideEffect : () -> Unit

interface Action : suspend () -> List<SideEffect>

interface Flow<A>

interface EventGenerator<X: Event> {
    val events: Flow<X>
}

interface Event

sealed interface AgentEvent : Event

sealed interface BBEvent : Event

interface BeliefBase<Query> : Collection<Belief>, EventGenerator<BBEvent> {
    fun select(query: Query): List<Belief>
}

sealed interface EnvEvent : Event

interface Process : EventGenerator<EnvEvent> {

}

interface Agent<Query : Any> : EventGenerator<AgentEvent> {
    val environment: Process
    val beliefBase: BeliefBase<Query>
    val plans: Collection<Plan<Query>>

    fun sense(): Event?

    fun deliberate(event: Event): List<Action>
}

class SetBB private constructor(private val backend: MutableSet<Belief>): BeliefBase<String>, MutableSet<Belief> by backend {
    override fun select(query: String): List<Belief> {
        TODO("Not yet implemented")
    }

    override val events: Flow<BBEvent>
        get() = TODO("Not yet implemented")
}

class MyAgent(environment: String): Agent<String> {
    override val beliefBase: SetBB = TODO()

    override fun sense() {
        beliefBase.add(object : Belief { })
    }

}

interface Belief

interface Plan<Query : Any> {

    val agent: Agent<Query>

    fun apply(event: Event): List<Action>
}

class LifeCycle {

    val agent: Agent<String> = TODO()

    var todos: List<Action>

    suspend fun runCycle() {
        val nextEvent = agent.sense()
        if (nextEvent != null) {
            todos += agent.deliberate(nextEvent)
        }
        val sideEffects = todos.first()()
        sideEffects.forEach { it() }
    }
}

action("pluto", 2) {
    val gg = do_something(argument.1, argument.2)
    super.side_effect(gg)
}

agent {
    believes {

    }
    goals {
        - start
    }
    plans {
        whenever(start) then {
            pluto(pippo, baudo)
        } causes {
            newPlan()
            messageSent()
        } onFailure {

        }
    }
}

