package it.unibo.jakta.context

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.intentions.MutableIntentionPool
import it.unibo.jakta.plans.Plan

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext<Query, Belief, Event, PlanType, ActivationRecordType, IntentionType> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>,
    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>,
    IntentionType: Intention<Query, Belief, Event, PlanType, ActivationRecordType>
{

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase<Query, Belief>

    /**
     * The collection of [Event] that the BDI Agent reacts on.
     *
     * As in Jason, Events are modeled with a FIFO queue. Users can provide an agent-specific event selection function,
     * that handle how an event is chosen from the queue.
     */
    val events: List<Event>

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: Collection<PlanType>

    val intentions: IntentionPool<Query, Belief, Event, ActivationRecordType, IntentionType, PlanType>
}

interface MutableAgentContext<
    Query,
    Belief,
    Event,
    PlanType,
    ActivationRecordType,
    IntentionType,
    out ImmutableContext
> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>,
    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>,
    IntentionType: Intention<Query, Belief, Event, PlanType, ActivationRecordType>,
    ImmutableContext: AgentContext<Query, Belief, Event, PlanType, ActivationRecordType, IntentionType>
{
     val mutableBeliefBase: MutableBeliefBase<Query, Belief, out BeliefBase<Query, Belief>>
     //fun addBelief(belief: Belief): Boolean
     //fun removeBelief(belief: Belief): Boolean

     val mutableEventList: MutableList<Event>
     //fun addEvent(event: Event): Boolean
     //fun removeEvent(event: Event): Boolean

     val mutablePlanLibrary: MutableCollection<out PlanType>
     //fun addPlan(plan: PlanType): Boolean
     //fun removePlan(plan: PlanType): Boolean

     val mutableIntentionPool: MutableIntentionPool<Query, Belief, Event, ActivationRecordType, IntentionType, PlanType>
     // fun removeIntention(intention: IntentionType): Boolean
     // fun updateIntention(intention: IntentionType): Boolean

    fun snapshot(): ImmutableContext
 }
