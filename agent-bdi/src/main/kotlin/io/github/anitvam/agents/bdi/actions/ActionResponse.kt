package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution

interface ActionResponse<C : SideEffect> {
    val substitution: Substitution
    val effects: Iterable<C>
}
