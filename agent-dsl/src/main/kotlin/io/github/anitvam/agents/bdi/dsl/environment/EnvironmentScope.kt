package io.github.anitvam.agents.bdi.dsl.environment

import io.github.anitvam.agents.bdi.dsl.Builder
import io.github.anitvam.agents.bdi.dsl.actions.ExternalActionsScope
import io.github.anitvam.agents.bdi.environment.Environment

class EnvironmentScope : Builder<Environment> {

    private val actionsScopes by lazy { ExternalActionsScope() }

    infix fun actions(actions: ExternalActionsScope.() -> Unit) {
        actionsScopes.also(actions)
    }

    override fun build(): Environment = Environment.of(
        externalActions = actionsScopes.build()
    )
}
