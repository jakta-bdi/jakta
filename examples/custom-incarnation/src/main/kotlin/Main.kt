import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.GuardScope
import kotlinx.coroutines.runBlocking

private data class Fact(val name: String, val args: List<Any>) {
    override fun toString() = "$name(${args.joinToString()})"
}

private fun fact(name: String, vararg args: Any) = Fact(name, args.toList())

private fun Fact.matches(name: String, arity: Int): List<Any>? = args.takeIf { this.name == name && args.size == arity }

private fun <Context : Any> GuardScope<Fact, Context>.believes(belief: Fact): Context? =
    context.takeIf { belief in beliefs }

private val greeter = agent<Fact, Fact, Any> {
    embodiedAs { Any() }
    believes {
        +fact("friend", "bob")
    }
    hasInitialGoals {
        !fact("greet", "bob")
        !fact("greet", "eve")
    }
    hasPlanLibrary {
        adding.goal {
            matches("greet", 1)
        } onlyWhen {
            believes(fact("friend", context[0]))
        } triggers {
            agent.print("Hello, ${context[0]}!")
        }
        failing.goal {
            matches("greet", 1)
        } triggers {
            agent.print("I don't greet strangers like ${context[0]}")
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
