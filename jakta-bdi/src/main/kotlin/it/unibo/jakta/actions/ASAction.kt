package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.Action
import it.unibo.jakta.plans.ActionTaskEffects
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface ASAction<C : ActionResult, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> : Action<Struct, ASBelief, Req, C> {
    val signature: Signature
    fun applySubstitution(substitution: Substitution)

    override suspend fun execute(intention: Intention<Struct, ASBelief>, argument: Req): ActionTaskEffects<C> =
        execute(argument)
    fun execute(request: Req): Res
    fun addResults(substitution: Substitution)
}
