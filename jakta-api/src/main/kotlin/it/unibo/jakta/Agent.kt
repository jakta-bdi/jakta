package it.unibo.jakta

import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator
import it.unibo.jakta.plans.Plan

interface Agent<Query : Any> : EventGenerator<Event.AgentEvent> {
    val agentID: AgentID
    val name: String
    val environment: AgentProcess
    val beliefBase: BeliefBase<Query>
    val plans: Collection<Plan<Query>>

    suspend fun sense(): Event?

    fun deliberate(event: Event): List<Action>
}

//interface Agent<
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
//> where
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
//{
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
//}
