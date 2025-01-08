package it.unibo.jakta.dsl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.Mas
import it.unibo.jakta.dsl.actions.ExternalActionScope
import it.unibo.jakta.dsl.actions.ExternalActionsScope
import it.unibo.jakta.dsl.actions.InternalActionScope
import it.unibo.jakta.dsl.actions.InternalActionsScope
import it.unibo.jakta.dsl.environment.EnvironmentScope
import it.unibo.jakta.dsl.plans.PlansScope
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.plans.Plan
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
fun environment(f: EnvironmentScope.() -> Unit): BasicEnvironment = EnvironmentScope().also(f).build()

@JaktaDSL
fun agent(name: String, f: AgentScope.() -> Unit): ASAgent = AgentScope(name).also(f).build()

fun plans(f: PlansScope.() -> Unit): Iterable<Plan> = PlansScope().also(f).build()

operator fun String.invoke(vararg terms: Term): Struct =
    Struct.of(this, *terms)

operator fun String.invoke(terms: List<Term>): Struct =
    Struct.of(this, terms)
