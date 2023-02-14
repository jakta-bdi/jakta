package io.github.anitvam.agents.bdi.actions.impl

import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.actions.InternalResponse
import io.github.anitvam.agents.bdi.context.ContextUpdate.ADDITION
import io.github.anitvam.agents.bdi.context.ContextUpdate.REMOVAL
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.actions.effects.BeliefChange
import io.github.anitvam.agents.bdi.actions.effects.IntentionChange
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.effects.EventChange
import io.github.anitvam.agents.bdi.actions.effects.Pause
import io.github.anitvam.agents.bdi.actions.effects.PlanChange
import io.github.anitvam.agents.bdi.actions.effects.Sleep
import io.github.anitvam.agents.bdi.actions.effects.Stop
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan
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
