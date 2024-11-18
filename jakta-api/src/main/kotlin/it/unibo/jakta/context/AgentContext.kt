package it.unibo.jakta.context

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan
import javax.management.Query

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext<Query, Belief, EventType, PlanType> where
    Query: Any,
    EventType: Event,
    PlanType: Plan<Query, Belief>
{

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase<Query, Belief>

    /**
     * The collection of [Event] that the BDI Agent reacts on.
     *
     * As in Jason, Events are modeled with a FIFO queue. Users can provide an agent-specific event selection function,
     * that handle how an event is chosen from the queue.
     */
    val events: List<EventType>

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: Collection<PlanType>

    val intentions: IntentionPool<Query, Belief>
}

interface MutableAgentContext<
    Query,
    Belief,
    EventType,
    PlanType,
    ActivationRecordType,
    IntentionType,
    out ImmutableContext
> where
    Query: Any,
    EventType: Event,
    PlanType: Plan<Query, Belief>,
    ActivationRecordType: ActivationRecord<Query, Belief>,
    IntentionType: Intention<Query, Belief, ActivationRecordType>,
    ImmutableContext: AgentContext<Query, Belief, EventType, PlanType>
{
     // val mutableBeliefBase: MutableBeliefBase<Query, Belief, out BeliefBase<Query, Belief>>
     fun addBelief(belief: Belief): Boolean
     fun removeBelief(belief: Belief): Boolean

     // val mutableEventList: MutableList<Event>
     fun addEvent(event: EventType): Boolean
     fun removeEvent(event: EventType): Boolean

     // val mutablePlanLibrary: MutableCollection<out Plan<Query, Belief>>
     fun addPlan(plan: PlanType): Boolean
     fun removePlan(plan: PlanType): Boolean

     // val intentionPool: MutableIntentionPool
     fun removeIntention(intention: IntentionType): Boolean
     fun updateIntention(intention: IntentionType): Boolean

    fun snapshot(): ImmutableContext
 }
