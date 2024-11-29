package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.environment.Environment
import it.unibo.tuprolog.solve.Signature

abstract class AbstractInternalAction(override val signature: Signature) :
    AbstractAction<InternalRequest>(signature) {

    override fun toString(): String = "InternalAction(${signature.name}, ${signature.arity})"
}