//package it.unibo.jakta.context
//
//import it.unibo.jakta.beliefs.BeliefBase
//import it.unibo.jakta.beliefs.MutableBeliefBase
//import it.unibo.jakta.intentions.ActivationRecord
//import it.unibo.jakta.intentions.Intention
//import it.unibo.jakta.intentions.IntentionPool
//import it.unibo.jakta.intentions.MutableIntentionPool
//import it.unibo.jakta.plans.ExecutionResult
//import it.unibo.jakta.plans.Plan
//import javax.management.Query
//
///**
// * The Context is the actual state of a BDI Agent's structures.
// */
//interface AgentContext<
//    Query,
//    Belief,
//    Event,
//    ActionResult: ExecutionResult<Any>,
//    Environment
//> where
//    Query: Any
//{
//    val environment: Environment
//
//    /** [BeliefBase] of the BDI Agent */
//    val beliefBase: BeliefBase<Query, Belief>
//
//    /**
//     * The collection of [Event] that the BDI Agent reacts on.
//     *
//     * As in Jason, Events are modeled with a FIFO queue. Users can provide an agent-specific event selection function,
//     * that handle how an event is chosen from the queue.
//     */
//    val events: List<Event>
//
//    /** [Plan]s collection of the BDI Agent */
//    val planLibrary: Collection<Plan<ActionResult>>
//
//    val intentions: IntentionPool<ActionResult>
//}
//
//interface MutableAgentContext<
//    Query: Any,
//    Belief,
//    Event,
//    ActionResult: ExecutionResult<Any>,
//    Environment
//> {
//
//    val mutableBeliefBase: MutableBeliefBase<Query, Belief, BeliefBase<Query, Belief>>
//    //fun addBelief(belief: Belief): Boolean
//    //fun removeBelief(belief: Belief): Boolean
//
//    val mutableEventList: MutableList<Event>
//    //fun addEvent(event: Event): Boolean
//    //fun removeEvent(event: Event): Boolean
//
//    val mutablePlanLibrary: MutableCollection<Plan<ActionResult>>
//    //fun addPlan(plan: PlanType): Boolean
//    //fun removePlan(plan: PlanType): Boolean
//
//    val mutableIntentionPool: MutableIntentionPool<ActionResult>
//    // fun removeIntention(intention: IntentionType): Boolean
//    // fun updateIntention(intention: IntentionType): Boolean
//
//    fun snapshot(): AgentContext<Query, Belief, Event, ActionResult, Environment>
//}
