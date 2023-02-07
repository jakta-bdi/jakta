package io.github.anitvam.agents.bdi.actions

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

abstract class InternalAction(override val signature: Signature) :
    AbstractAction<AgentChange, InternalResponse, InternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected fun addBelief(belief: Belief) = effects.add(BeliefChange(belief, ADDITION))
    protected fun removeBelief(belief: Belief) = effects.add(BeliefChange(belief, REMOVAL))
    protected fun addIntention(intention: Intention) = effects.add(IntentionChange(intention, ADDITION))
    protected fun removeIntention(intention: Intention) = effects.add(IntentionChange(intention, REMOVAL))
    protected fun addEvent(event: Event) = effects.add(EventChange(event, ADDITION))
    protected fun removeEvent(event: Event) = effects.add(EventChange(event, REMOVAL))
    protected fun addPlan(plan: Plan) = effects.add(PlanChange(plan, ADDITION))
    protected fun removePlan(plan: Plan) = effects.add(PlanChange(plan, REMOVAL))
    protected fun stopAgent() = effects.add(Stop())
    protected fun sleepAgent(millis: Long) = effects.add(Sleep(millis))
    protected fun pauseAgent() = effects.add(Pause())

    override fun toString(): String = "InternalAction(${signature.name}, ${signature.arity})"
}
