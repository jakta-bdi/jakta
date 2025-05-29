package it.unibo.jakta

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.plans.Matcher
import it.unibo.jakta.plans.Plan

interface Agent<Belief : Any, Query : Any, Response> {
    // val environment: AgentProcess

    interface Context<Belief : Any, Query : Any, Response> :
        EventGenerator<Event.Internal.Goal<Belief, Query, Response>> {
        val agentID: AgentID get() = AgentID()
        val agentName: String get() = "Agent-$agentID"
        val beliefBase: BeliefBase<Belief>
        val plans: Collection<Plan<Belief, Query, Response>>

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
    }

    fun sense(agentProcess: AgentProcess): Event.Internal?

    context(_: Matcher<Belief, Query, Response>)
    fun deliberate(agentProcess: AgentProcess, event: Event.Internal)

    fun act(agentProcess: AgentProcess)
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
