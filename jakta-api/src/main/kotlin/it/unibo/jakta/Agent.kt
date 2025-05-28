package it.unibo.jakta

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator
import it.unibo.jakta.plans.Plan

interface Agent {
    // val environment: AgentProcess

    interface Context<Belief : Any, Query : Any, Result> : EventGenerator<Event.Internal.Goal<Belief, Query, Result>> {
        val agentID: AgentID get() = AgentID()
        val agentName: String get() = "Agent-$agentID"
        val beliefBase: BeliefBase<Belief, Query, Result>
        val plans: Collection<Plan<Belief, Query, Result>>
    }

    fun sense(agentProcess: AgentProcess): Event.Internal?

    fun deliberate(agentProcess: AgentProcess, event: Event.Internal)

    fun act(agentProcess: AgentProcess)
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
