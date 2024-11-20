package it.unibo.jakta.actions.impl

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.PlanChange
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.solve.Signature

abstract class AbstractInternalAction(override val signature: Signature) : InternalAction,
    AbstractAction<AgentChange, InternalResponse, InternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    final override suspend fun execute(argument: InternalRequest): InternalResponse {
        if (argument.arguments.size > signature.arity) {
            throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
        }
        action(argument)
        val res = argument.reply(result, effects.toMutableList())
        effects.clear()
        // ======================
        if (res.substitution.isSuccess) {
            val intention = argument.agent.context.snapshot().intentions.nextIntention()
            if (intention.recordStack.isNotEmpty()) {
                intention.applySubstitution(internalResponse.substitution)
            }
            val newContext = applyEffects(context, internalResponse.effects)
            ExecutionResult(
                newContext.copy(intentions = newContext.intentions.updateIntention(newIntention)),
            )
        } else {
            ExecutionResult(failAchievementGoal(intention, context))
        }

        // ======================
        return res
    }

    override fun addBelief(belief: ASBelief) {
        effects.add(BeliefChange(belief))
    }

    override fun removeBelief(belief: ASBelief) {
        effects.add(BeliefChange(belief))
    }

    override fun addIntention(intention: ASIntention) {
        effects.add(IntentionChange(intention))
    }

    override fun removeIntention(intention: ASIntention) {
        effects.add(IntentionChange(intention))
    }

    override fun addEvent(event: ASEvent) {
        effects.add(EventChange(event))
    }

    override fun removeEvent(event: ASEvent) {
        effects.add(EventChange(event))
    }

    override fun addPlan(plan: ASPlan) {
        effects.add(PlanChange(plan))
    }

    override fun removePlan(plan: ASPlan) {
        effects.add(PlanChange(plan))
    }

    override fun stopAgent() {
        effects.add(Stop)
    }

    override fun sleepAgent(millis: Long) {
        effects.add(Sleep(millis))
    }

    override fun pauseAgent() {
        effects.add(Pause)
    }

    override fun toString(): String = "InternalAction(${signature.name}, ${signature.arity})"
}
