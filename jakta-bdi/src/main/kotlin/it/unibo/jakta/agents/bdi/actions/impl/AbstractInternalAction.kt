package it.unibo.jakta.agents.bdi.actions.impl

import it.unibo.jakta.agents.bdi.actions.InternalAction
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.InternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.actions.effects.BeliefChange
import it.unibo.jakta.agents.bdi.actions.effects.EventChange
import it.unibo.jakta.agents.bdi.actions.effects.IntentionChange
import it.unibo.jakta.agents.bdi.actions.effects.Pause
import it.unibo.jakta.agents.bdi.actions.effects.PlanChange
import it.unibo.jakta.agents.bdi.actions.effects.Sleep
import it.unibo.jakta.agents.bdi.actions.effects.Stop
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.context.ContextUpdate.ADDITION
import it.unibo.jakta.agents.bdi.context.ContextUpdate.REMOVAL
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.tuprolog.solve.Signature

abstract class AbstractInternalAction(override val signature: Signature) : InternalAction,
    AbstractAction<AgentChange, InternalResponse, InternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    override fun addBelief(belief: Belief) {
        effects.add(BeliefChange(belief, ADDITION))
    }

    override fun removeBelief(belief: Belief) {
        effects.add(BeliefChange(belief, REMOVAL))
    }

    override fun addIntention(intention: Intention) {
        effects.add(IntentionChange(intention, ADDITION))
    }

    override fun removeIntention(intention: Intention) {
        effects.add(IntentionChange(intention, REMOVAL))
    }

    override fun addEvent(event: Event) {
        effects.add(EventChange(event, ADDITION))
    }

    override fun removeEvent(event: Event) {
        effects.add(EventChange(event, REMOVAL))
    }

    override fun addPlan(plan: Plan) {
        effects.add(PlanChange(plan, ADDITION))
    }

    override fun removePlan(plan: Plan) {
        effects.add(PlanChange(plan, REMOVAL))
    }

    override fun stopAgent() {
        effects.add(Stop())
    }

    override fun sleepAgent(millis: Long) {
        effects.add(Sleep(millis))
    }

    override fun pauseAgent() {
        effects.add(Pause())
    }

    override fun toString(): String = "InternalAction(${signature.name}, ${signature.arity})"
}
