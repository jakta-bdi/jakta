package it.unibo.jakta.dsl.environment

import it.unibo.jakta.dsl.Builder
import it.unibo.jakta.dsl.actions.ExternalActionsScope
import it.unibo.jakta.environment.Environment

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
