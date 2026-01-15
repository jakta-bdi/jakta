package it.unibo.jakta.agent

import it.unibo.jakta.environment.Environment
import it.unibo.jakta.event.Event
import it.unibo.jakta.plan.Plan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel

///**
// * Represents a generic agent in the MAS.
// */
//interface Agent<Belief : Any, Goal : Any> : SendChannel<Event> {
//    /**
//     * The name of the agent.
//     */
//    val name: String
//
//    /**
//     * The *beliefs* currently held by the agent.
//     */
//    val beliefs: Collection<Belief>
//
//    /**
//     * The plans available to handle belief-related events.
//     */
//    val beliefPlans: List<Plan.Belief<Belief, Goal, *, *, *>>
//
//    /**
//     * The plans available to handle goal-related events.
//     */
//    val goalPlans: List<Plan.Goal<Belief, Goal, *, *, *>>
//
//    /**
//     * Mapping function which defines how to convert a [Event.External] into a [Event.Internal].
//     * The implementation of this mapping function is specific to each [Agent] specialization,
//     * since [Event.External] types can be application-specific.
//     */
//    val eventMappingFunction: Event.External.() -> Event.Internal?
//
//    // TODO all of the stuff above should probably move to AgentActions
//    //  the stuff below should stay separate instead as it is internal
//
//    /**
//     * Runs a step of a reasoning cycle, suspends until an event is available and process it.
//     * Can launch plans in response to the event.
//     * @param scope must be a SupervisorScope
//     */
//    suspend fun step(scope: CoroutineScope)
//
//    /**
//     * Stops the agent, cancelling its main Job.
//     */
//    suspend fun stop()
//}

interface Agent<Belief: Any, Goal: Any, Skills: Any, Body: AgentBody> :
    RunnableAgent<Belief, Goal, Skills>,
    EnvironmentAgent<Body>
