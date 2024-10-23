package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.tuprolog.core.Struct

/**
 * [Task.ActionExecution] which adds a [Belief] into the [BeliefBase]
 */
data class AddBelief(override val activity: PrologBelief) : Task.ActionExecution<PrologBelief>

/**
 * [Task.ActionExecution] which removes a [Belief] into the [BeliefBase]
 */
data class RemoveBelief(override val activity: PrologBelief) : Task.ActionExecution<PrologBelief>

/**
 * [Task.ActionExecution] which updates the [Belief]'s content in the [BeliefBase]
 */
data class UpdateBelief(override val activity: PrologBelief) : Task.ActionExecution<PrologBelief>

/**
 * [Task.ActionExecution] which executes an [Action], first looks for an internal action and then for an external one.
 */
data class Act(override val activity: Struct) : Task.ActionExecution<Struct>

/**
 * [Task.ActionExecution] which executes an [InternalAction].
 */
data class ActInternally(override val activity: Struct) : Task.ActionExecution<Struct>

/**
 * [Task.ActionExecution] which executes an [ExternalAction].
 */
data class ActExternally(override val activity: Struct) : Task.ActionExecution<Struct>
