package it.unibo.jakta.plans

import it.unibo.jakta.actions.effects.SideEffect
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.beliefs.PrologMutableBeliefBase
import it.unibo.jakta.context.AgentContext
import it.unibo.tuprolog.core.Struct

data class NewBeliefBase(val beliefBase: PrologBeliefBase) : Task.ExecutionResult
interface BeliefTask: Task<NewBeliefBase>

/**
 * [Task.ActionExecution] which adds a [Belief] into the [BeliefBase]
 */
data class AddBelief(val belief: PrologBelief, val beliefBaseProvider: () -> PrologMutableBeliefBase) : BeliefTask {
    override fun execute(): NewBeliefBase = NewBeliefBase(
        beliefBaseProvider().apply { add(belief) }.snapshot()
    )
}

/**
 * [Task.ActionExecution] which removes a [Belief] into the [BeliefBase]
 */
data class RemoveBelief(override val activity: PrologBelief) : Task.ActionExecution<PrologBelief> {
    override fun <Query : Any, Belief, BB : BeliefBase<Query, Belief, BB>> execute(agentContext: AgentContext<Query, Belief, BB>): List<SideEffect> {
        TODO("Not yet implemented")
    }
}

/**
 * [Task.ActionExecution] which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(override val activity: PrologBelief) : Task.ActionExecution<PrologBelief> {
    override fun <Query : Any, Belief, BB : BeliefBase<Query, Belief, BB>> execute(agentContext: AgentContext<Query, Belief, BB>): List<SideEffect> {
        TODO("Not yet implemented")
    }
}

/**
 * [Task.ActionExecution] which executes an [Action], first looks for an internal action and then for an external one.
 */
data class Act(override val activity: Struct) : Task.ActionExecution<Struct> {
    override fun <Query : Any, Belief, BB : BeliefBase<Query, Belief, BB>> execute(agentContext: AgentContext<Query, Belief, BB>): List<SideEffect> {
        TODO("Not yet implemented")
    }
}

/**
 * [Task.ActionExecution] which executes an [InternalAction].
 */
data class ActInternally(override val activity: Struct) : Task.ActionExecution<Struct> {
    override fun execute() {
        TODO("Not yet implemented")
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
