package io.github.anitvam.agents.bdi.goals.actions

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

sealed interface Action<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
}

object Print : ImperativeInternalAction("print", 1) {
    override fun InternalRequest.action() {
        println(arguments.first())
        addBelief()
        result = Substitution.empty()
    }
}

fun main() {
    val x = object : ImperativeInternalAction("print", 1) {
        override fun InternalRequest.action() {
            println(arguments.first())
        }
    }

//    val y = InternalAction.unary("print") {
//        println(it)
//        reply()
//
//    }
    x.execute(InternalRequest.of(Atom.of("BANANA")))
}
