package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.dsl.actions.ExternalActionScope
import io.github.anitvam.agents.bdi.dsl.actions.ExternalActionsScope
import io.github.anitvam.agents.bdi.dsl.actions.InternalActionScope
import io.github.anitvam.agents.bdi.dsl.actions.InternalActionsScope
import io.github.anitvam.agents.bdi.dsl.environment.EnvironmentScope
import io.github.anitvam.agents.bdi.dsl.plans.PlansScope
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term

@DslMarker
annotation class JaktaDSL

@JaktaDSL
fun mas(f: MasScope.() -> Unit): Mas =
    MasScope().also(f).build()
fun internalAction(name: String, arity: Int, f: InternalActionScope.() -> Unit) =
    InternalActionsScope().newAction(name, arity, f)

fun externalAction(name: String, arity: Int, f: ExternalActionScope.() -> Unit) =
    ExternalActionsScope().newAction(name, arity, f)

@JaktaDSL
fun environment(f: EnvironmentScope.() -> Unit): Environment {
    return EnvironmentScope().also(f).build()
}

fun plans(f: PlansScope.() -> Unit): Iterable<Plan> = PlansScope().also(f).build()

operator fun String.invoke(vararg terms: Term): Struct =
    Struct.of(this, *terms)

operator fun String.invoke(terms: List<Term>): Struct =
    Struct.of(this, terms)
