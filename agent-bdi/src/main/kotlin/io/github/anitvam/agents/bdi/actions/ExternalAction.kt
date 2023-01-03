package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.tuprolog.solve.Signature

abstract class ExternalAction(override val signature: Signature) :
    AbstractAction<EnvironmentChange, ExternalResponse, ExternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))
}
