import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.GuardScope
import kotlinx.coroutines.runBlocking

private fun Int.even(): Int? = takeIf { it > 0 && it % 2 == 0 }

private fun Int.odd(): Int? = takeIf { it > 0 && it % 2 == 1 }

private fun GuardScope<Int, Int>.explored(): Int? = context.takeIf { it in beliefs }

private fun GuardScope<Int, Int>.unexplored(): Int? = context.takeIf { it !in beliefs }

private val collatz = agent<Int, Int, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !10
    }
    hasPlanLibrary {
        adding.goal {
            takeIf { it > 0 }
        } onlyWhen {
            explored()
        } triggers {
            agent.print("Back to $context: the sequence loops, as the conjecture says")
            node.terminateNode()
        }
        adding.goal {
            even()
        } onlyWhen {
            unexplored()
        } triggers {
            agent.believe(context)
            agent.print("$context is even: halving it")
            agent.achieve(context / 2)
        }
        adding.goal {
            odd()
        } onlyWhen {
            unexplored()
        } triggers {
            agent.believe(context)
            agent.print("$context is odd: tripling it and adding one")
            agent.achieve(3 * context + 1)
        }
    }
}

/**
 * Runs the Collatz agent.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node { withAgents(collatz) }
    }.runLocally()
}
