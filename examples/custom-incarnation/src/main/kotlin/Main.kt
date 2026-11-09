import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import kotlinx.coroutines.runBlocking

private const val COLLATZ_START = 10
private const val SIEVE_LIMIT = 30

/**
 * Runs the Collatz agent, then the prime sieve: two agents whose beliefs and goals are plain numbers.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node { withAgents(collatz(COLLATZ_START)) }
    }.runLocally()
    mas(NodeBuilders.baseNode()) {
        node { withAgents(primeSieve(SIEVE_LIMIT)) }
    }.runLocally()
}
