package it.unibo.jakta.plans

import it.unibo.jakta.actions.ExternalAction
import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.impl.AbstractInternalAction
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.Intention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Signature


abstract class AbstractBeliefInternalAction(
    var belief: ASBelief,
    name: String,
): AbstractInternalAction(Signature(name, 1)) {
    override fun applySubstitution(substitution: Substitution) {
        belief = belief.applySubstitution(substitution)
    }
}

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
class AddBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "addBelief") {
    override fun action(request: InternalRequest) {
        request.agent.context.addBelief(belief)
    }
}


/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
class RemoveBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "addBelief")  {
    override fun action(request: InternalRequest) {
        if ((request.agent.context).removeBelief(belief)) {
            removeBelief(BeliefBaseRemoval(belief))
        }
    }
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
class UpdateBelief(
    belief: ASBelief,
) : AbstractBeliefInternalAction(belief, "removeBelief")  {
    override fun action(request: InternalRequest) {
        TODO("Missing implementation for update in beliefcontext")
    }
}

/**
 * [Task.ActionExecution] which executes an [InternalAction].
 */
class ActInternally(
    var parameters: List<Term>,
) : AbstractInternalAction(
    Signature("actInternally", parameters.size)
) {
    constructor(vararg parameter: Term): this(parameter.toList())

    override fun applySubstitution(substitution: Substitution) {
        parameters = parameters.map { it.apply(substitution) } // TODO("Needs testing")
    }

     override fun action(request: InternalRequest) {
         if (parameters.filterIsInstance<Var>().isNotEmpty())
             return this.failAchievementGoal(intention, context))
         var runningIntention = request.agent.context.snapshot().intentions.nextTask()
         val internalResponse = action.execute(
             InternalRequest.of(agent, controller?.currentTime(), goal.args),
         )
         // Apply substitution
         return if (internalResponse.substitution.isSuccess) {
             if (newIntention.recordStack.isNotEmpty()) {
                 newIntention = newIntention.applySubstitution(internalResponse.substitution)
             }
             val newContext = applyEffects(context, internalResponse.effects)
             ExecutionResult(
                 newContext.copy(intentions = newContext.intentions.updateIntention(newIntention)),
             )
         } else {
             ExecutionResult(failAchievementGoal(intention, context))
         }
    }

}

/**
 * [Task.ActionExecution] which executes an [ExternalAction].
 */
data class ActExternally(override val activity: Struct) : Task.ActionExecution<Struct> {
    override fun execute() {
        TODO("Not yet implemented")
    }
}

private fun executeInternalAction(
    intention: ASIntention,
    action: InternalAction,
    context: ASAgentContext,
    actionInvocation: Struct,
): Event {

}

private fun executeExternalAction(
    intention: Intention,
    action: ExternalAction,
    context: AgentContext,
    environment: Environment,
    goal: ActionGoal,
): Event {
    var newIntention = intention.pop()
    if (action.signature.arity < goal.action.args.size) {
        // Argument number mismatch from action definition
        return ExecutionResult(failAchievementGoal(intention, context))
    } else {
        val externalResponse = action.execute(
            ExternalRequest.of(
                environment,
                agent.name,
                controller?.currentTime(),
                goal.action.args,
            ),
        )
        return if (externalResponse.substitution.isSuccess) {
            if (newIntention.recordStack.isNotEmpty()) {
                newIntention = newIntention.applySubstitution(externalResponse.substitution)
            }
            ExecutionResult(
                context.copy(intentions = context.intentions.updateIntention(newIntention)),
                externalResponse.effects,
            )
        } else {
            ExecutionResult(failAchievementGoal(intention, context))
        }
    }
}
