package io.github.anitvam.agents.bdi.dsl.environment

import io.github.anitvam.agents.bdi.dsl.Builder
import io.github.anitvam.agents.bdi.dsl.actions.ExternalActionsScope
import io.github.anitvam.agents.bdi.environment.Environment

class EnvironmentScope : Builder<Environment> {

    private val actionsScopes by lazy { ExternalActionsScope() }
    private var environment = Environment.of()

    infix fun actions(actions: ExternalActionsScope.() -> Unit) {
        actionsScopes.also(actions)
    }

    fun from(environment: Environment) {
        this.environment = environment
    }

    override fun build(): Environment = environment.copy(
        externalActions = actionsScopes.build(),
    )
}
