package it.unibo.jakta.agents.bdi.dsl.beliefs

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

context(LogicProgrammingScope)
fun Struct.source(name: String): Struct = addFirst(Struct.of("source", Atom.of(name)))

context(LogicProgrammingScope)
fun String.source(name: String): Struct = atomOf(this).addFirst(Struct.of("source", Atom.of(name)))

context(LogicProgrammingScope)
val Struct.fromSelf
    get() = source("self")

context(LogicProgrammingScope)
val String.fromSelf
    get() = source("self")

context(LogicProgrammingScope)
val Struct.fromPercept
    get() = source("percept")

context(LogicProgrammingScope)
val String.fromPercept
    get() = source("percept")
