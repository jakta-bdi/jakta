package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.beliefs.RetrieveResult
import it.unibo.jakta.agents.bdi.context.AgentContext
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.events.EventQueue
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionResult
import it.unibo.jakta.agents.bdi.impl.AgentLifecycleImpl
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.jakta.agents.bdi.intentions.IntentionPool
import it.unibo.jakta.agents.bdi.intentions.SchedulingResult
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.fsm.Activity

/** BDI Agent definition*/
interface AgentLifecycle {
    /**
     * STEP 1 of reasoning cycle: Belief Update Function.
     * This function defines how to merge new [perceptions] into the current [beliefBase]
     * @param perceptions: [BeliefBase] that collects all agent's perceptions of the environment
     * @param beliefBase: [BeliefBase] the current agent's [BeliefBase]
     * @return a [RetrieveResult] that contains the updated [BeliefBase] and the added [Belief]s
     */
    fun updateBelief(
        perceptions: BeliefBase,
        beliefBase: BeliefBase,
    ): RetrieveResult

    /**
     * STEP 5 of reasoning cycle: Selecting an Event.
     * This function select an event to be handled in a particular reasoning cycle.
     * The default implementation follows a FIFO policy for the [EventQueue].
     * @param events: [EventQueue] on which select the event
     * @return the selected [Event]
     */
    fun selectEvent(events: EventQueue): Event?

    /**
     * STEP 6 of reasoning cycle: Retrieving all Relevant Plans.
     * This function returns all plans from [PlanLibrary] that have a triggering event that can be unified
     * with the selected event.
     * @param event: the selected [Event]
     * @param planLibrary: the [PlanLibrary] of the Agent
     * @return the relevant [Plan]s
     */
    fun selectRelevantPlans(
        event: Event,
        planLibrary: PlanLibrary,
    ): PlanLibrary

    /**
     * STEP 7 of reasoning cycle: Determining the Applicable Plans.
     * This function defines if a plan is applicable based on the agent's Belief Base.
     * @param event: the selected [Event] that triggered the [Plan]
     * @param plan: the triggered [Plan]
     * @param beliefBase: the agent's [BeliefBase]
     * @return yes if it's applicable, false otherwise.
     */
    fun isPlanApplicable(
        event: Event,
        plan: Plan,
        beliefBase: BeliefBase,
    ): Boolean

    /**
     * Step 8 of reasoning cycle: Selecting one Applicable Plan.
     * Given all the applicable plans, this Selection Function returns the plan that the agent will commit to execute.
     * By default,
     * @param plans: applicable [Plan]s
     * @return the selected [Plan] to be executed
     */
    fun selectApplicablePlan(plans: Iterable<Plan>): Plan?

    /**
     * Step 8 of reasoning cycle: Assign selected plan to an Intention.
     * If the event is external, then a new Intention is created. Otherwise, the selected plan is pushed on top of the
     * firing Intention.
     * @param event: the [Event] that triggered the [Plan]
     * @param plan: the selected [Plan]
     * @param intentions: the [IntentionPool] of the agent
     * @return the updated [Intention]
     */
    fun assignPlanToIntention(
        event: Event,
        plan: Plan,
        intentions: IntentionPool,
    ): Intention

    /**
     * Step 9 of reasoning cycle: Selecting an Intention for Further Execution.
     * Given all agent's intentions, this Selection Function selects the intention to be scheduled to execution
     * by the agent. By default, this function implements Round Robin scheduling.
     * @param intentions: the agent's [IntentionPool]
     * @return a [SchedulingResult] with the updated [IntentionPool] and the [Intention] to execute
     */
    fun scheduleIntention(intentions: IntentionPool): SchedulingResult

    /**
     * Step 10 of reasoning cycle: Executing One step of an Intention.
     * Depending on the formula on the top of the intention, the agent will execute the related action.
     * @param intention: [Intention] on which the agent is currently focused
     * @return the updated [Intention] after agent execution
     */
    fun runIntention(
        intention: Intention,
        context: AgentContext,
        environment: Environment,
    ): ExecutionResult

    /** Performs the whole procedure (10 steps) of the BDI Agent's Reasoning Cycle.
     *  @param environment the [Environment]
     *  @param controller [Activity.Controller] that manages agent's execution
     *  @param debugEnabled [Boolean] specifies wether debug logs are needed or not
     */
    fun runOneCycle(
        environment: Environment,
        controller: Activity.Controller? = null,
        debugEnabled: Boolean = false,
    ): Iterable<EnvironmentChange> {
        sense(environment, controller, debugEnabled)
        deliberate()
        return act(environment)
    }

    /**
     * Performs the sensing phase of the reasoning cycle, in particular:
     *  - STEP1: Perceive the Environment
     *  - STEP2: Update the BeliefBase
     *  - STEP3: Receiving Communication from Other Agents
     *  - STEP4: Selecting "Socially Acceptable" Messages
     *  @param environment the [Environment]
     *  @param controller [Activity.Controller] that manages agent's execution
     *  @param debugEnabled [Boolean] specifies wether debug logs are needed or not
     */
    fun sense(
        environment: Environment,
        controller: Activity.Controller?,
        debugEnabled: Boolean,
    )

    /** Performs the reason phase of the reasoning cycle, in particular:
     *  - STEP5: Selecting an Event
     *  - STEP6: Retrieving all Relevant Plans
     *  - STEP7: Determining the Applicable Plans
     *  - STEP8: Selecting one Applicable Plan
     */
    fun deliberate()

    /**
     * Performs the reason phase of the reasoning cycle, in particular:
     *  - STEP9: Select an Intention for Further Execution
     *  - STEP10: Executing one Step on an Intention
     *  @param environment [Environment]
     *  @return an Iterable of [EnvironmentChange] that need to be scheduled for the application in the environment.
     */
    fun act(environment: Environment): Iterable<EnvironmentChange>

    companion object {
        fun newLifecycleFor(agent: Agent): AgentLifecycle = AgentLifecycleImpl(agent)
    }
}
