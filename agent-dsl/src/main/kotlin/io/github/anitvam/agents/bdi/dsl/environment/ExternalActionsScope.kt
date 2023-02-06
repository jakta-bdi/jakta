package io.github.anitvam.agents.bdi.dsl.environment

import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.dsl.Builder
import it.unibo.tuprolog.core.Term
import kotlin.reflect.KProperty

class ExternalActionsScope : Builder<Map<String, ExternalAction>> {

    private val actions = mutableListOf<ExternalAction>()

    fun action(name: String, arity: Int, f: ExternalRequest.() -> Unit) {
        val action = object : ExternalAction(name, arity) {
            override fun ExternalRequest.action() {
                f()
            }
        }
        actions += action
    }

    override fun build(): Map<String, ExternalAction> {
        TODO()
    }

    @Suppress("UNCHECKED_CAST")
    public data class ArgumentGetterDelegate<T : Term>(val index: Int) {
        operator fun getValue(thisRef: ExternalRequest, property: KProperty<*>): T {
            return thisRef.arguments[index] as T
        }
    }

    fun <T : Term> ExternalRequest.argument(index: Int): ArgumentGetterDelegate<T> {
        return ArgumentGetterDelegate(index)
    }

    // action("pippo", 2) {
    //     val a: Struct by argument(0)
    // }
}
