package it.unibo.jakta.actions

import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.actions.responses.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.tuprolog.solve.Signature

abstract class AbstractInternalAction(override val signature: Signature) :
    AbstractAction<AgentChange, InternalResponse, InternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    fun addActionEffect(actionResult: AgentChange) = effects.add(actionResult)

    override fun toString(): String = "InternalAction(${signature.name}, ${signature.arity})"
}
