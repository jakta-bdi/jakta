package io.github.anitvam.agents.bdi.dsl.actions

import io.github.anitvam.agents.bdi.actions.Action
import io.github.anitvam.agents.bdi.actions.ActionRequest
import io.github.anitvam.agents.bdi.actions.ActionResponse
import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import io.github.anitvam.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Term
import kotlin.reflect.KProperty

abstract class ActionsScope<
    C : SideEffect,
    Res : ActionResponse<C>,
    Req : ActionRequest<C, Res>,
    A : Action<C, Res, Req>,
    As : ActionScope<C, Res, Req, A>
    > : Builder<Map<String, A>> {

    private val actions = mutableListOf<A>()

    fun action(name: String, arity: Int, f: As.() -> Unit) {
        actions += newAction(name, arity, f)
    }

    protected abstract fun newAction(name: String, arity: Int, f: As.() -> Unit): A

    override fun build(): Map<String, A> = actions.associateBy { it.signature.name }

    inner class ArgumentGetterDelegate<T : Term>(private val index: Int) {
        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Req?, property: KProperty<*>): T {
            println("Getting argument $thisRef") // null
            return thisRef?.arguments?.get(index) as T
        }
    }
    @Suppress("UNCHECKED_CAST")
    fun <T : Term> Req.argument(index: Int): T {
        // return ArgumentGetterDelegate(index)
        return this.arguments[index] as T
    }
}
