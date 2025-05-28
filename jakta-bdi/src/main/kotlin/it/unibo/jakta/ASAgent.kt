package it.unibo.jakta

import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.Event
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.impl.AgentImpl
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.ASMutableIntentionPool
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface ASAgent : Agent, Taggable<ASAgent> {

    val controller: Activity.Controller?
    val context: ASAgentContext

    interface ASAgentContext : Agent.Context<ASBelief, Struct, Solution> {
        /** [BeliefBase] of the BDI Agent */
        override val beliefBase: ASBeliefBase

        /** [Plan]s collection of the BDI Agent */
        override val plans: Collection<PlanImpl>
        val intentions: ASIntentionPool
    }

    interface ASMutableAgentContext : ASAgentContext {
        override val beliefBase: ASMutableBeliefBase
        override val plans: MutableCollection<PlanImpl>
        override val intentions: ASMutableIntentionPool

        fun enqueue(event: Event.Internal.Goal<ASBelief, Struct, Solution>)

        fun drop(event: Event.Internal.Goal<ASBelief, Struct, Solution>)
    }

    /**
     * STEP 5 of reasoning cycle: Selecting an Event.
     * This function select an event to be handled in a particular reasoning cycle.
     * The default implementation follows a FIFO policy for the list of [Event]s.
     * @param events: list of [Event]s on which select the event
     * @return the selected [Event]
     */
    fun selectEvent(environment: BasicEnvironment): Event?

    /**
     * STEP 6 of reasoning cycle: Retrieving all Relevant [Plan]s.
     * This function returns all [Plan]s that have a triggering event that can be unified
     * with the selected event.
     * @param event: the selected [Event]
     * @param planLibrary: the [Plan]s known by the Agent
     * @return the relevant [Plan]s
     */
    fun selectRelevantPlans(event: Event.Internal, planLibrary: List<Plan<ASBelief, Struct, Solution>>): List<Plan<ASBelief, Struct, Solution>>

    /**
     * STEP 7 of reasoning cycle: Determining the Applicable Plans.
     * This function defines if a plan is applicable based on the agent's Belief Base.
     * @param event: the selected [Event] that triggered the [Plan]
     * @param plan: the triggered [Plan]
     * @param beliefBase: the agent's [BeliefBase]
     * @return yes if it's applicable, false otherwise.
     */
    fun isPlanApplicable(
        event: Event.Internal,
        plan: Plan<ASBelief, Struct, Solution>,
        beliefBase: ASBeliefBase,
    ): Boolean

    /**
     * Step 8 of reasoning cycle: Selecting one Applicable Plan.
     * Given all the applicable plans, this Selection Function returns the plan that the agent will commit to execute.
     * By default,
     * @param plans: applicable [Plan]s
     * @return the selected [Plan] to be executed
     */
    fun selectApplicablePlan(plans: List<Plan<ASBelief, Struct, Solution>>): Plan<ASBelief, Struct, Solution>?

    /**
     * Step 8 of reasoning cycle: Assign selected plan to an Intention.
     * If the event is external, then a new Intention is created. Otherwise, the selected plan is pushed on top of the
     * firing Intention.
     * @param event: the [ASEvent] that triggered the [Plan]
     * @param plan: the selected [Plan]
     * @param intentions: the [IntentionPool] of the agent
     * @return the updated [Intention] or null if the intention is not found
     */
    fun assignPlanToIntention(
        event: Event.Internal,
        plan: Plan<ASBelief, Struct, Solution>,
        intentions: ASIntentionPool,
    ): ASIntention?

    /**
     * Step 9 of reasoning cycle: Selecting an Intention for Further Execution.
     * Given all agent's intentions, this Selection Function selects the intention to be scheduled to execution
     * by the agent. By default, this function implements Round Robin scheduling.
     * @param intentions: the agent's [IntentionPool]
     * @return the [Intention] to execute, or null if there's no intention
     */
    fun scheduleIntention(): ASIntention?

    /**
     * Step 10 of reasoning cycle: Executing One step of an Intention.
     * Depending on the formula on the top of the intention, the agent will execute the related action.
     * @param intention: [Intention] on which the agent is currently focused
     */
    fun runIntention(
        intention: ASIntention,
    ): List<SideEffect>

    fun sense(environment: BasicEnvironment, debugEnabled: Boolean): Event.Internal?

    fun deliberate(environment: BasicEnvironment, event: Event.Internal, debugEnabled: Boolean)

    fun act(environment: BasicEnvironment, debugEnabled: Boolean)

    companion object {
        fun empty(controller: Activity.Controller? = null): ASAgent = AgentImpl(controller)

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
            events: List<Event.Internal.Goal<ASBelief, Struct, Solution>> = emptyList(),
            planLibrary: MutableCollection<Plan<ASBelief, Struct, Solution>> = mutableListOf(),
            controller: Activity.Controller? = null,
            // internalActions: MutableMap<String, InternalAction> = ExecutionActions.default(), //TODO()
        ): ASAgent = AgentImpl(
            controller,
            agentID,
            name,
            events,
            beliefBase,
            planLibrary,
        )
    }
}
