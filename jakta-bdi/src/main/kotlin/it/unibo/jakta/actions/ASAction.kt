package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.plans.Action
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface ASAction<C, Res, Req> : Action<Struct, ASBelief, Req, C> where
    Req: ActionRequest<C, Res>,
    Res: ActionResponse<C>,
    C: ActionResult
{
    val signature: Signature

    fun applySubstitution(substitution: Substitution)

    override suspend fun execute(argument: Req): Res

    fun addResults(substitution: Substitution)
}
