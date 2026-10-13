import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import kotlinx.coroutines.runBlocking

/**
 * An agent with a single goal, `sayHello`, and a plan that prints a greeting and stops the node.
 * Beliefs and goals are plain strings, so it only needs `jakta-core`.
 */
val helloWorldAgent = agent<String, String, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !"sayHello"
    }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "sayHello" }
        } triggers {
            agent.print("Hello, world!")
            node.terminateNode()
        }
    }
}

/**
 * Entrypoint of the hello-world application.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node { withAgents(helloWorldAgent) }
    }.runLocally()
}
