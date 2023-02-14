package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.dsl.actions.ExternalActionScope
import io.github.anitvam.agents.bdi.dsl.actions.ExternalActionsScope
import io.github.anitvam.agents.bdi.dsl.actions.InternalActionScope
import io.github.anitvam.agents.bdi.dsl.actions.InternalActionsScope

fun mas(f: MasScope.() -> Unit): Mas =
    MasScope().also(f).build()

fun internalAction(name: String, arity: Int, f: InternalActionScope.() -> Unit) =
    InternalActionsScope().newAction(name, arity, f)

fun externalAction(name: String, arity: Int, f: ExternalActionScope.() -> Unit) =
    ExternalActionsScope().newAction(name, arity, f)
