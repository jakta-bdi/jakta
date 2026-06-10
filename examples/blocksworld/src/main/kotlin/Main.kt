import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlinx.coroutines.runBlocking

fun main() {
    Logger.setMinSeverity(Severity.Assert)
    val world = BlocksWorld(0)
    val desiredWorldState = initialGoal {
        "state"(logicListOf(
                logicListOf(blockA, blockB),
                logicListOf(blockC, blockD, blockE, blockF))
            )
    }
    runBlocking {
        mas(LocalNodeBuilder()) {
            // TODO I don't like this
            blocksWorldNode(world, desiredWorldState)
        }.run(CoroutineNodeRunner())
    }
}
