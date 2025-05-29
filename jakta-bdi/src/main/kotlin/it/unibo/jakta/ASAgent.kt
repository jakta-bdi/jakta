package it.unibo.jakta

import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.Event
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.impl.AgentImpl
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.ASMutableIntentionPool
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface ASAgent :
    Agent<ASBelief, Struct, Solution>,
    Taggable<ASAgent> {

    var controller: Activity.Controller?
    val context: ASAgentContext

    interface ASAgentContext : Agent.Context<ASBelief, Struct, Solution> {
        /** [BeliefBase] of the BDI Agent */
        override val beliefBase: ASBeliefBase

        /** [Plan]s collection of the BDI Agent */
        override val plans: Collection<Plan<ASBelief, Struct, Solution>>
        val intentions: ASIntentionPool
    }

    interface ASMutableAgentContext : ASAgentContext {
        override val beliefBase: MutableBeliefBase
        override val plans: MutableCollection<Plan<ASBelief, Struct, Solution>>
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
    fun runIntention(intention: ASIntention): List<SideEffect>

    fun sense(environment: BasicEnvironment): Event.Internal?

    fun act(environment: BasicEnvironment)

    companion object {
        fun empty(controller: Activity.Controller? = null): ASAgent = AgentImpl(controller)

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: MutableBeliefBase = MutableBeliefBase.empty(),
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
