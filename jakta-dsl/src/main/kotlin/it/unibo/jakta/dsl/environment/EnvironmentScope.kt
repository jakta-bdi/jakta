package it.unibo.jakta.dsl.environment

import it.unibo.jakta.dsl.Builder
import it.unibo.jakta.dsl.actions.ExternalActionsScope
import it.unibo.jakta.environment.BasicEnvironment

class EnvironmentScope : Builder<BasicEnvironment> {

    private val actionsScopes by lazy { ExternalActionsScope() }
    private var environment = BasicEnvironment.of()

    infix fun actions(actions: ExternalActionsScope.() -> Unit) {
        actionsScopes.also(actions)
    }

    fun from(environment: BasicEnvironment) {
        this.environment = environment
    }

    override fun build(): BasicEnvironment = environment.copy(
        externalActions = actionsScopes.build(),
    )
}
