package it.unibo.jakta.mas

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.environment.EnvironmentContext
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Implementation of a [MAS].
 */
data class MASImpl<Belief : Any, Goal : Any, Env : Environment>(
    override val environment: Env,
    override val agents: Set<Agent<Belief, Goal, Env>>,
) : MAS<Belief, Goal, Env> {
    private val log =
        Logger(
            Logger.config,
            "MAS",
        )

    override suspend fun run() = supervisorScope {
        val environmentContext = EnvironmentContext(environment)
        agents
            .map { agent ->
                log.d { "Launching agent ${agent.name}" }
                launch(environmentContext) {
                    supervisorScope {
                        log.d { "Agent ${agent.name} started" }
                        while (true) {
                            log.d { "Running one step of Agent ${agent.name}" }
                            agent.step(this)
                        }
                    }
                }
            }.joinAll()
    }
}
