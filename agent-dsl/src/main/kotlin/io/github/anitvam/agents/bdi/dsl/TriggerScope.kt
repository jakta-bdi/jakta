package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.events.Trigger
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct

class TriggerScope(private val scope: Scope, private val trigger: Struct) {

    infix fun iff(guards: GuardScope.() -> Struct): TriggerGuardScope {
        val guard = GuardScope(scope).let(guards)
        return TriggerGuardScope(scope, trigger, guard)
    }

    infix fun then(body: BodyScope.() -> Unit): TriggerGuardScope {
        TODO()
    }
}