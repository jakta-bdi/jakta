package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.actions.responses.ExternalResponse
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.tuprolog.solve.Signature

abstract class AbstractExternalAction(override val signature: Signature) :
    AbstractInternalAction<Any, ActionSideEffect<Any>, ExternalResponse, ExternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    fun addActionResult(actionResult: EnvironmentChange) = effects.add(actionResult)

    override fun toString(): String = "ExternalAction(${signature.name}, ${signature.arity})"
}
