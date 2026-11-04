import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.GuardScope
import kotlinx.coroutines.runBlocking

private data class Entry(val key: String, val value: Any)

private data class Task(val action: String, val target: String)

private fun Task.targetOf(action: String): String? = target.takeIf { this.action == action }

private inline fun <reified V> GuardScope<Entry, *>.valueOf(key: String): V? =
    beliefs.firstOrNull { it.key == key }?.value as? V

private val greeter = agent<Entry, Task, Any> {
    embodiedAs { Any() }
    believes {
        +Entry("friends", setOf("bob"))
    }
    hasInitialGoals {
        !Task("greet", "bob")
        !Task("greet", "eve")
    }
    hasPlanLibrary {
        adding.goal {
            targetOf("greet")
        } onlyWhen {
            context.takeIf { it in valueOf<Set<String>>("friends").orEmpty() }
        } triggers {
            agent.print("Hello, $context!")
        }
        failing.goal {
            targetOf("greet")
        } triggers {
            agent.print("I don't greet strangers like $context")
            node.terminateNode()
        }
    }
}

/**
 * Runs the greeter.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node { withAgents(greeter) }
    }.runLocally()
}
