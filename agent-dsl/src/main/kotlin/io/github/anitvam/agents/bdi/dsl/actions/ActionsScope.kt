package io.github.anitvam.agents.bdi.dsl.actions

import io.github.anitvam.agents.bdi.actions.Action
import io.github.anitvam.agents.bdi.actions.ActionRequest
import io.github.anitvam.agents.bdi.actions.ActionResponse
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.actions.ExternalResponse
import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.actions.InternalResponse
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import io.github.anitvam.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Term
import kotlin.reflect.KProperty

abstract class ActionsScope<
    C : SideEffect,
    Res : ActionResponse<C>,
    Req : ActionRequest<C, Res>,
    A : Action<C, Res, Req>
    > : Builder<Map<String, A>> {

    private val actions = mutableListOf<A>()

    fun action(name: String, arity: Int, f: Req.() -> Unit) {
        actions += newAction(name, arity, f)
    }

    protected abstract fun newAction(name: String, arity: Int, f: Req.() -> Unit): A

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

class InternalActionsScope : ActionsScope<AgentChange, InternalResponse, InternalRequest, InternalAction>() {

    override fun newAction(name: String, arity: Int, f: InternalRequest.() -> Unit): InternalAction {
        return object : InternalAction(name, arity) {
            override fun InternalRequest.action() {
                f()
            }
        }
    }
}

class ExternalActionsScope : ActionsScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction>() {
    override fun newAction(name: String, arity: Int, f: ExternalRequest.() -> Unit): ExternalAction {
        return object : ExternalAction(name, arity) {
            override fun ExternalRequest.action() {
                f()
            }
        }
    }
}

class SampleClass {
    val m: String by SampleDelegate()
}
class SampleDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, Delegating '${property.name}' to me!"
    }
}

fun main() {
    val s = SampleClass()
    println(s.m)
}
