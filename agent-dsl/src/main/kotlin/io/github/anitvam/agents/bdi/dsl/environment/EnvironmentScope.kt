package io.github.anitvam.agents.bdi.dsl.environment

import io.github.anitvam.agents.bdi.dsl.Builder
import io.github.anitvam.agents.bdi.environment.Environment

class EnvironmentScope : Builder<Environment> {

    private val actionsScopes = mutableListOf<ExternalActionsScope>()

    infix fun actions(actions: ExternalActionsScope.() -> Unit) {
        actionsScopes += ExternalActionsScope().also(actions)
    }

    override fun build(): Environment {
        TODO("Not yet implemented")
    }
}
