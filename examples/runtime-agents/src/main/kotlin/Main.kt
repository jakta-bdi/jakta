import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.terminate
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private val workerID = BaseAgentID("Worker")

private val worker = agent<String, String, Any>(workerID) {
    embodiedAs { Any() }
    hasInitialGoals { !"work" }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "work" }
        } triggers {
            agent.print("Worker started")
            delay(10.seconds)
            agent.print("Worker done") // never printed: the worker is removed before
        }
    }
}

private val quitter = agent<String, String, Any>(BaseAgentID("Quitter")) {
    embodiedAs { Any() }
    hasInitialGoals { !"quit" }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "quit" }
        } triggers {
            agent.print("Bye!")
            with(node) { agent.terminate() }
        }
    }
}

/**
 * A manager adding and removing a worker.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(quitter)
            agent<String, String>(BaseAgentID("Manager")) {
                embodiedAs { Any() }
                hasInitialGoals { !"manage" }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "manage" }
                    } triggers {
                        node.addAgent(worker)
                        delay(1.seconds)
                        agent.print("Agents: ${node.agents.keys.map { it.displayName }}")
                        node.removeAgent(workerID)
                        delay(1.seconds)
                        agent.print("Agents: ${node.agents.keys.map { it.displayName }}")
                        node.terminateNode()
                    }
                }
            }
        }
    }.runLocally()
}
