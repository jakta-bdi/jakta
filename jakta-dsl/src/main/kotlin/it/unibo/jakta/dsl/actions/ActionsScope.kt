package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.actions.ActionRequest
import it.unibo.jakta.actions.ActionResponse
import it.unibo.jakta.actions.effects.SideEffect
import it.unibo.jakta.dsl.Builder
import it.unibo.tuprolog.core.Term
import kotlin.reflect.KFunction

abstract class ActionsScope<C, Res, Req, A, As> : Builder<Map<String, A>>
    where C : SideEffect,
          Res : ActionResponse<C>,
          Req : ActionRequest<C, Res>,
          A : Action<C, Res, Req>,
          As : ActionScope<C, Res, Req, A> {

    private val actions = mutableListOf<A>()

    fun action(name: String, arity: Int, f: As.() -> Unit) {
        actions += newAction(name, arity, f)
    }

    fun action(method: KFunction<*>) {
        actions += newAction(method.name, method.parameters.size) {
            method.call(*arguments.toTypedArray())
        }
    }

    fun action(action: A) {
        actions += action
    }

    protected abstract fun newAction(name: String, arity: Int, f: As.() -> Unit): A

    override fun build(): Map<String, A> = actions.associateBy { it.signature.name }

    @Suppress("UNCHECKED_CAST")
    fun <T : Term> Req.argument(index: Int): T {
        // return ArgumentGetterDelegate(index)
        return this.arguments[index] as T
    }
}
