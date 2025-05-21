package it.unibo.jakta

import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.plans.ASPlan

/** BDI Agent definition*/
interface ASAgentLifecycle {

    val agent: ASAgent

    /**
     * STEP 1 of reasoning cycle: Belief Update Function.
     * This function defines how to merge new [perceptions] into the current [beliefBase]
     * @param perceptions: [BeliefBase] that collects all agent's perceptions of the environment
     * @param beliefBase: [MutableBeliefBase] the current agent's beliefs where the perceptions will be added.
     * @return true is the [MutableBeliefBase] is changed as a result of the operation
     */
    fun updateBelief(
        perceptions: ASBeliefBase,
        beliefBase: ASMutableBeliefBase,
    ): Boolean

    /**
     * STEP 5 of reasoning cycle: Selecting an Event.
     * This function select an event to be handled in a particular reasoning cycle.
     * The default implementation follows a FIFO policy for the list of [Event]s.
     * @param events: list of [Event]s on which select the event
     * @return the selected [Event]
     */
    fun selectEvent(events: List<ASEvent>): ASEvent?

    /**
     * STEP 6 of reasoning cycle: Retrieving all Relevant [Plan]s.
     * This function returns all [Plan]s that have a triggering event that can be unified
     * with the selected event.
     * @param event: the selected [Event]
     * @param planLibrary: the [Plan]s known by the Agent
     * @return the relevant [Plan]s
     */
    fun selectRelevantPlans(event: ASEvent, planLibrary: List<ASPlan>): List<ASPlan>

    /**
     * STEP 7 of reasoning cycle: Determining the Applicable Plans.
     * This function defines if a plan is applicable based on the agent's Belief Base.
     * @param event: the selected [Event] that triggered the [Plan]
     * @param plan: the triggered [Plan]
     * @param beliefBase: the agent's [BeliefBase]
     * @return yes if it's applicable, false otherwise.
     */
    fun isPlanApplicable(
        event: ASEvent,
        plan: ASPlan,
        beliefBase: ASBeliefBase,
    ): Boolean

    /**
     * Step 8 of reasoning cycle: Selecting one Applicable Plan.
     * Given all the applicable plans, this Selection Function returns the plan that the agent will commit to execute.
     * By default,
     * @param plans: applicable [Plan]s
     * @return the selected [Plan] to be executed
     */
    fun selectApplicablePlan(plans: List<ASPlan>): ASPlan?

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
        event: ASEvent,
        plan: ASPlan,
        intentions: ASIntentionPool,
    ): ASIntention?

    /**
     * Step 9 of reasoning cycle: Selecting an Intention for Further Execution.
     * Given all agent's intentions, this Selection Function selects the intention to be scheduled to execution
     * by the agent. By default, this function implements Round Robin scheduling.
     * @param intentions: the agent's [IntentionPool]
     * @return the [Intention] to execute, or null if there's no intention
     */
    fun scheduleIntention(
        intentions: ASIntentionPool
    ): ASIntention?

    /**
     * Step 10 of reasoning cycle: Executing One step of an Intention.
     * Depending on the formula on the top of the intention, the agent will execute the related action.
     * @param intention: [Intention] on which the agent is currently focused
     */
    fun runIntention(
        intention: ASIntention,
        agent: ASAgent,
        environment: AgentProcess,
    )

    /** Performs the whole procedure (10 steps) of the BDI Agent's Reasoning Cycle.
     *  @param environment the [AgentProcess]
     *  @return true if the environment has been changed as a result of this operation
     */
    fun runOneCycle(
        environment: AgentProcess,
        //controller: Activity.Controller? = null,
        debugEnabled: Boolean = false,
    ): Boolean {
        sense(environment)
        deliberate()
        return act(environment)
    }

    /**
     * Performs the sensing phase of the reasoning cycle, in particular:
     *  - STEP1: Perceive the Environment
     *  - STEP2: Update the ASBeliefBase
     *  - STEP3: Receiving Communication from Other Agents
     *  - STEP4: Selecting "Socially Acceptable" Messages
     *  @param environment the [AgentProcess]
     */
    fun sense(environment: AgentProcess): Event?

    /** Performs the reason phase of the reasoning cycle, in particular:
     *  - STEP5: Selecting an ASEvent
     *  - STEP6: Retrieving all Relevant Plans
     *  - STEP7: Determining the Applicable Plans
     *  - STEP8: Selecting one Applicable Plan
     */
    fun deliberate()

    /**
     * Performs the reason phase of the reasoning cycle, in particular:
     *  - STEP9: Select an Intention for Further Execution
     *  - STEP10: Executing one Step on an Intention
     *  @param environment [AgentProcess]
     *  @return true if the environment has been changed as a result of this operation.
     */
    fun act(environment: AgentProcess): Boolean
}

