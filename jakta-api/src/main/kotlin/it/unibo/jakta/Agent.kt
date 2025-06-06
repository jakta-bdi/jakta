package it.unibo.jakta

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.intentions.MutableIntentionPool
import it.unibo.jakta.resolution.Matcher
import it.unibo.jakta.plans.Plan
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface  Agent<Belief : Any, Query : Any, Response> {
    // val environment: AgentProcess

    val context: Context<Belief, Query, Response>
    val timeProvider: TimeProvider

    fun sense(agentProcess: AgentProcess<Belief>): Event.Internal?

    context(_: Matcher<Belief, Query, Response>)
    fun deliberate(agentProcess: AgentProcess<Belief>, event: Event.Internal)

    suspend fun act(agentProcess: AgentProcess<Belief>)

    fun interface TimeProvider {
        @OptIn(ExperimentalTime::class)
        fun currentTime(): Instant
    }

    interface Context<Belief : Any, Query : Any, Response> :
        EventGenerator<Event.Internal.Goal<Belief, Query, Response>> {
        val agentID: AgentID get() = AgentID()
        val agentName: String get() = "Agent-$agentID"
        val beliefBase: BeliefBase<Belief>
        val plans: List<Plan<Belief, Query, Response>>
        val intentions: IntentionPool<Belief, Query, Response>

        /**
         * STEP 6 of reasoning cycle: Retrieving all Relevant [Plan]s.
         * This function returns all [Plan]s that have a triggering event that can be unified
         * with the selected event.
         * @param event: the selected [Event]
         * @param planLibrary: the [Plan]s known by the Agent
         * @return the relevant [Plan]s
         */
        context(matcher: Matcher<Belief, Query, Response>)
        fun activationRecordFor(event: Event.Internal): ActivationRecord<Belief, Query, Response>? =
            matcher.matchPlanFor(event, plans, beliefBase)

        interface Mutable<Belief : Any, Query : Any, Response> : Context<Belief, Query, Response> {
            override val beliefBase: MutableBeliefBase<Belief>
            override val plans: MutableList<Plan<Belief, Query, Response>>
            override val intentions: MutableIntentionPool<Belief, Query, Response>

            fun enqueue(event: Event.Internal.Goal<Belief, Query, Response>)

            fun drop(event: Event.Internal.Goal<Belief, Query, Response>)
        }
    }
}

// interface Agent<
//    Query,
//    Belief,
//    BeliefBaseType,
//    MutableBeliefBaseType,
//    Event,
//    PlanType,
//    ActivationRecordType,
//    IntentionType,
//    ImmutableContext,
//    Environment
// > where
//    Query: Any,
//    PlanType: Plan<Query, Belief, Event>,
//    BeliefBaseType: BeliefBase<Query, Belief>,
//    MutableBeliefBaseType: MutableBeliefBase<Query, Belief, BeliefBaseType>,
//    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>,
//    IntentionType: Intention<Query, Belief, Event, PlanType, ActivationRecordType>,
//    ImmutableContext: AgentContext<
//        Query,
//        Belief,
//        BeliefBaseType,
//        Event,
//        PlanType,
//        ActivationRecordType,
//        IntentionType,
//        Environment
//    >
// {
//    val agentID: AgentID
//
//    val name: String
//
//    val lifecycle: AgentLifecycle<
//        Query,
//        Belief,
//        BeliefBaseType,
//        MutableBeliefBaseType,
//        Event,
//        PlanType,
//        ActivationRecordType,
//        IntentionType,
//        ImmutableContext,
//        Environment
//    >
//
//    /** Agent's Actual State */
//    val context: MutableAgentContext<
//        Query,
//        Belief,
//        BeliefBaseType,
//        MutableBeliefBaseType,
//        Event,
//        PlanType,
//        ActivationRecordType,
//        IntentionType,
//        ImmutableContext,
//        Environment
//    >
//
//
// }
