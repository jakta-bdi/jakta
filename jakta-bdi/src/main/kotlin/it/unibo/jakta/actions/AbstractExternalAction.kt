package it.unibo.jakta.actions

import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.tuprolog.solve.Signature

abstract class AbstractExternalAction(override val signature: Signature) :
    AbstractAction<ExternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected abstract val environment: BasicEnvironment

    override fun toString(): String = "ExternalAction(${signature.name}, ${signature.arity})"
}
