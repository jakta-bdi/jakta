import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlinx.coroutines.runBlocking
import model.Block
import ui.goalOf

/**
 * Connects to the youBot of `worlds/blocksworld.wbt`, which Webots must have open, and lets the Blocks World agent
 * build the towers in [args], e.g. `A,B,C` for one tower with A at the bottom, or `A,B C` for two towers.
 */
fun main(args: Array<String>) = runBlocking {
    Logger.setMinSeverity(Severity.Assert) // only what the agent prints
    val goal = args.ifEmpty { arrayOf("A,B,C") }.map { tower -> tower.split(",").map(::Block) }
    val webots = Webots()
    mas(NodeBuilders.baseNode()) {
        blocksWorldNode(goalOf(goal)) { WebotsBlocksWorldSkills(webots, it) }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
