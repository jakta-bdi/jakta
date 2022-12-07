package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.ContextUpdate.ADDITION
import io.github.anitvam.agents.bdi.ContextUpdate.REMOVAL
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.actions.effects.BeliefChange
import io.github.anitvam.agents.bdi.goals.actions.effects.IntentionChange
import io.github.anitvam.agents.bdi.goals.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.goals.actions.effects.EventChange
import io.github.anitvam.agents.bdi.goals.actions.effects.PlanChange
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class InternalAction(override val signature: Signature) :
    Action<AgentChange, InternalResponse, InternalRequest> {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<AgentChange> = mutableListOf()

    override fun execute(request: InternalRequest): InternalResponse {
        request.action()
        return request.reply(result, effects)
    }

    abstract fun InternalRequest.action()

    protected fun addBelief(belief: Belief) = effects.add(BeliefChange(belief, ADDITION))
    protected fun removeBelief(belief: Belief) = effects.add(BeliefChange(belief, REMOVAL))
    protected fun addIntention(intention: Intention) = effects.add(IntentionChange(intention, ADDITION))
    protected fun removeIntention(intention: Intention) = effects.add(IntentionChange(intention, REMOVAL))
    protected fun addEvent(event: Event) = effects.add(EventChange(event, ADDITION))
    protected fun removeEvent(event: Event) = effects.add(EventChange(event, REMOVAL))
    protected fun addPlan(plan: Plan) = effects.add(PlanChange(plan, ADDITION))
    protected fun removePlan(plan: Plan) = effects.add(PlanChange(plan, REMOVAL))
}
