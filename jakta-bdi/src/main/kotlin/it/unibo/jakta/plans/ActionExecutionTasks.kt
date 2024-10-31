package it.unibo.jakta.plans

import it.unibo.jakta.actions.ExternalAction
import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.Event
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.jakta.intentions.Intention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.concurrentSolverFactory
import it.unibo.tuprolog.solve.sideffects.SideEffect

interface PrologActionTask: Action { // ????? TODO("missing implementations")
    fun applySubstitution(substitution: Substitution): Action
}

/**
 * [Action] Task which adds a [Belief] into the [BeliefBase]
 */
data class AddBelief(val belief: ASBelief, val beliefBaseProvider: () -> ASMutableBeliefBase) : Action, ASTask<ActionTaskEffects, AddBelief> {
    override suspend fun execute(): ActionTaskEffects {
        val operationResult = beliefBaseProvider().add(belief)
        return object : ActionTaskEffects {
            // TODO("Better ways to hide the anonym implementation?")
            override val events: List<Event> = when(operationResult) {
                true -> listOf(BeliefBaseAddition(belief))
                false -> listOf()
            }
        }
    }

    override fun applySubstitution(substitution: Substitution): AddBelief {
        this.copy(belief = belief.applySubstitution(substitution))
    }
}

/**
 * [Action] which removes a [Belief] from the [BeliefBase]
 */
data class RemoveBelief(val belief: ASBelief, val beliefBaseProvider: () -> ASMutableBeliefBase) : Action {
    override suspend fun execute(): ActionTaskEffects {
        val operationResult = beliefBaseProvider().remove(belief)
        return object : ActionTaskEffects {
            override val events: List<Event> = when (operationResult) {
                true -> listOf(BeliefBaseRemoval(belief))
                else -> listOf()
            }
        }
    }
}

/**
 * [Action] Task which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(val belief: ASBelief, val beliefBaseProvider: () -> ASMutableBeliefBase) : Action {
    override suspend fun execute(): ActionTaskEffects {
        val operationResult = beliefBaseProvider().update(belief)
        return object : ActionTaskEffects {
            override val events: List<Event> = when (operationResult) {
                true -> listOf() //TODO("Ci andrebbbe un beliefbase update.. oppure una rimozione e aggiunta")
                else -> listOf()
            }
        }
    }
}

/**
 * [Task.ActionExecution] which executes an [Action], first looks for an internal action and then for an external one.
 */
data class Act(
    val actionSignature: Struct,
    val actionsProvider: () -> Map<String, ASAction<*,*,*>>,
    val currentIntentionProvider: () -> Intention,
    val mutableContextProvider: () -> ASMutableAgentContext,
) : Action {
    override suspend fun execute(): ActionTaskEffects {
        val action = actionsProvider()[actionSignature.functor]
        val currentIntention = currentIntentionProvider()
        val events: List<Event> = if (action == null) {
//            if (debugEnabled) {
//                println("[${agent.name}] WARNING: ${nextGoal.action.functor} Action not found.")
//            }
            listOf(AchievementGoalFailure(currentIntention.currentPlan(), currentIntention))
        } else {
            // Execute Action
            when (action) {
                is InternalAction -> listOf(executeInternalAction(currentIntention, action, mutableContextProvider(), nextGoal))
                is ExternalAction -> listOf(executeExternalAction(currentIntention, action, mutableContextProvider(), environment, nextGoal))
                else -> listOf()
                    // throw IllegalStateException("The Action to execute is neither internal nor external")
                    // TODO("Manage this kind of error with logs (?)")
            }
        }
        return object : ActionTaskEffects {
            override val events: List<Event>
                get() = events

        }
    }
    }
        object : ActionTaskEffects {
            init {


            override val events: List<Event> = {}
        }
    }
}

/**
 * [Task.ActionExecution] which executes an [InternalAction].
 */
data class ActInternally(
    val actionSignature: Struct,
    val actionsProvider: () -> Map<String, ExternalAction>,
    val currentIntentionProvider: () -> Intention,
) : Action {
    override suspend fun execute(): ActionTaskEffects {
        val internalAction = actionsProvider()[actionSignature.functor]

        val events: List<ASEvent> = if (internalAction == null) {
            // Internal Action not found
//            if (debugEnabled) {
//                println("[${agent.name}] WARNING: ${nextGoal.action.functor} Internal Action not found.")
//            }
            listOf(AchievementGoalFailure(actionSignature, ))
        } else {
            // Execute Internal Action
            executeInternalAction(intention, internalAction, context, nextGoal)
        }
        return object : ActionTaskEffects{
            override val events: List<Event>
                get() = TODO("Not yet implemented")
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
    intention: Intention,
    action: InternalAction,
    context: AgentContext,
    goal: ActionGoal,
): Event {
    var newIntention = intention.pop()
    if (action.signature.arity < goal.action.args.size) { // Arguments number mismatch
        return ExecutionResult(failAchievementGoal(intention, context))
    } else {
        val internalResponse = action.execute(
            InternalRequest.of(this.agent, controller?.currentTime(), goal.action.args),
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
