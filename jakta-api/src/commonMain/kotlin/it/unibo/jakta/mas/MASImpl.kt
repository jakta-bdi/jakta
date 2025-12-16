package it.unibo.jakta.mas

import co.touchlab.kermit.Logger
import it.unibo.jakta.environment.Environment
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Implementation of a [MAS].
 */
data class MASImpl<Belief : Any, Goal : Any, Env : Environment<Belief, Goal>>(
    override val environment: Env,
) : MAS<Belief, Goal, Env> {
    private val log =
        Logger(
            Logger.config,
            "MAS",
        )

    override suspend fun run() = supervisorScope {
        environment.agents.map { agent ->
            // Running Agents
            log.d { "Launching agent ${agent.name}" }
            launch {
                supervisorScope {
                    log.d { "Agent ${agent.name} started" }
                    while (true) {
                        log.d { "Running one step of Agent ${agent.name}" }
                        agent.step(this)
                    }
                }
            }

            // Start Perceiving
            launch {
                while(true) {
                    environment.processEvent()
                }
            }

        }.joinAll()
    }
}
