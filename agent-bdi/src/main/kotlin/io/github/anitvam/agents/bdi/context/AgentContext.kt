package io.github.anitvam.agents.bdi.context

import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.actions.InternalActions
import io.github.anitvam.agents.bdi.context.impl.AgentContextImpl
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.plans.Plan

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase

    /** [Event]s on which the BDI Agent reacts */
    val events: EventQueue

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: PlanLibrary

    val intentions: IntentionPool

    val internalActions: Map<String, InternalAction>

    fun copy(
        beliefBase: BeliefBase = this.beliefBase,
        events: EventQueue = this.events,
        planLibrary: PlanLibrary = this.planLibrary,
        intentions: IntentionPool = this.intentions,
        internalActions: Map<String, InternalAction> = this.internalActions,
    ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, internalActions, intentions)

    companion object {
        fun of(
            beliefBase: BeliefBase = BeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, internalActions)
    }
}
