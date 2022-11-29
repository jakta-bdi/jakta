package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.actions.impl.InternalActionImpl
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.Signature

interface InternalAction : Action<AgentChange, InternalResponse, InternalRequest> {
    companion object {
        fun of(name: String, arity: Int, action: InternalRequest.() -> InternalResponse): InternalAction =
            InternalActionImpl(Signature(name, arity), action)

        fun unary(name: String, action: InternalRequest.(Term) -> Unit): InternalAction =
            InternalActionImpl(Signature(name, 1)) {
                action(arguments[0])
                reply()
            }

        fun binary(name: String, action: InternalRequest.(Term, Term) -> Unit): InternalAction =
            InternalActionImpl(Signature(name, 2)) {
                action(arguments[0], arguments[1])
                reply()
            }
    }
}

abstract class ImperativeInternalAction(override val signature: Signature) : InternalAction {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<AgentChange> = mutableListOf()

    override fun execute(request: InternalRequest): InternalResponse {
        request.action()
        return request.reply(result, effects)
    }

    abstract fun InternalRequest.action()

    fun addBelief() {
        effects.add(AddBelief)
    }
}
