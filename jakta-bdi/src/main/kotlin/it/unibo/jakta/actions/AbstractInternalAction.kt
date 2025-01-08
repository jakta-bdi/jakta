package it.unibo.jakta.actions

import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.tuprolog.solve.Signature

abstract class AbstractInternalAction(override val signature: Signature) :
    AbstractAction<InternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    override fun toString(): String = "InternalAction(${signature.name}, ${signature.arity})"
}