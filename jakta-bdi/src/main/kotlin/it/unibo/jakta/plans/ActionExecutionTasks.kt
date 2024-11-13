package it.unibo.jakta.plans

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.ExternalAction
import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.impl.AbstractAction
import it.unibo.jakta.actions.impl.AbstractInternalAction
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.context.Removal
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.Event
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.Intention
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.concurrentSolverFactory
import it.unibo.tuprolog.solve.sideffects.SideEffect

/**
 * [Action] Task which adds a [ASBelief] into the [ASBeliefBase]
 */
class AddBelief(
    var belief: ASBelief,
) : AbstractInternalAction(
    Signature("addBelief", 1)
) {

    override fun applySubstitution(substitution: Substitution) {
        belief = belief.applySubstitution(substitution)
    }

    override fun action(request: InternalRequest) {
        if ((request.agent.context as ASMutableAgentContext).addBelief(belief)) {
            addBelief(BeliefBaseAddition(belief))
        }
    }
}



/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
data class RemoveBelief(
    val belief: ASBelief,
    override var currentIntentionProvider: () -> ASIntention = { ASIntention.of() },
    override var agentContextProvider: () -> ASMutableAgentContext = { ASMutableAgentContext.of() },
    override var environmentProvider: () -> Environment = { Environment.of() },
) : Action,  ASTask<ActionTaskEffects, RemoveBelief>  {
    override suspend fun execute(): ActionTaskEffects {
        val operationResult = agentContextProvider().removeBelief(belief)
        return object : ActionTaskEffects {
            override val events: List<Event> = when (operationResult) {
                true -> listOf(BeliefBaseRemoval(belief))
                else -> listOf()
            }
        }
    }

    override fun applySubstitution(substitution: Substitution): RemoveBelief =
        this.copy(belief = belief.applySubstitution(substitution))

}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(
    val belief: ASBelief,
    override var currentIntentionProvider: () -> ASIntention = { ASIntention.of() },
    override var agentContextProvider: () -> ASMutableAgentContext = { ASMutableAgentContext.of() },
    override var environmentProvider: () -> Environment = { Environment.of() },
) : Action, ASTask<ActionTaskEffects, UpdateBelief>  {
    override suspend fun execute(): ActionTaskEffects {
        //val operationResult = agentContextProvider().updateBelief(belief)
        // TODO ("Missing implementation for update in beliefcontext")
        return object : ActionTaskEffects {
            override val events: List<Event> = when (true) {
                true -> listOf() //TODO("Ci andrebbbe un beliefbase update.. oppure una rimozione e aggiunta")
                else -> listOf()
            }
        }
    }

    override fun applySubstitution(substitution: Substitution): UpdateBelief =
        this.copy(belief = belief.applySubstitution(substitution))
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
        parameters = parameters.map { it.apply(substitution) } // TODO("Needs better verification")
    }

     override fun action(request: InternalRequest) {
         if (parameters.filterIsInstance<Var>().isNotEmpty())
             return this.failAchievementGoal(intention, context))
         executeInternalAction(
            request.agent.context.intention //TODO,
            internalAction, context, nextGoal)
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
    var newIntention = intention.pop()
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
