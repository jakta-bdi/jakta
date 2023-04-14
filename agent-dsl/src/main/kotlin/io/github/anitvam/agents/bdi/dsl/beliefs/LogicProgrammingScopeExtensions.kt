package io.github.anitvam.agents.bdi.dsl.beliefs

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

context(LogicProgrammingScope)
fun Struct.source(name: String): Struct = addFirst(Struct.of("source", Atom.of(name)))

context(LogicProgrammingScope)
val Struct.fromSelf
    get() = source("self")

context(LogicProgrammingScope)
val Struct.fromPercept
    get() = source("percept")
