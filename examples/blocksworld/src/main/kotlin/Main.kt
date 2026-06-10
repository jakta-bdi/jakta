import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlinx.coroutines.runBlocking

fun main() {
    Logger.setMinSeverity(Severity.Warn)
    val world = BlocksWorld(123)
    runBlocking {
        mas(LocalNodeBuilder()) {
            // TODO I don't like this
            blocksWorldNode(world)
        }.run(CoroutineNodeRunner())
    }
}
