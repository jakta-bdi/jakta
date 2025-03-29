package it.unibo.tuprolog.dsl.jakta

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.BaseLogicProgrammingScope

interface LogicProgrammingScopeWithJaktaExtensions<S : LogicProgrammingScopeWithJaktaExtensions<S>> :
    BaseLogicProgrammingScope<S> {
    fun Struct.source(name: String): Struct = addFirst(Struct.of("source", Atom.of(name)))

    fun String.source(name: String): Struct = atomOf(this).addFirst(Struct.of("source", Atom.of(name)))

    val Struct.fromSelf
        get() = source("self")

    val String.fromSelf
        get() = source("self")

    val Struct.fromPercept
        get() = source("percept")

    val String.fromPercept
        get() = source("percept")
}
