import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.node.CoroutineNodeRunner
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.Block
import model.BlocksWorld
import ui.BlocksWorldApp
import ui.BlocksWorldAppState
import ui.BlocksWorldScreen
import ui.BlocksWorldViewModel

//fun main() {
//    Logger.setMinSeverity(Severity.Assert)
//    val world = BlocksWorld(0)
//    val desiredWorldState = initialGoal {
//        "state"(logicListOf(
//                logicListOf(blockA, blockB),
//                logicListOf(blockC, blockD, blockE, blockF))
//            )
//    }
//    runBlocking {
//        mas(LocalNodeBuilder()) {
//            blocksWorldNode(world, desiredWorldState)
//        }.run(CoroutineNodeRunner())
//    }
//}

fun main() = application {

    Logger.setMinSeverity(Severity.Assert)

    val appState = remember { BlocksWorldAppState()}


    Window(
        onCloseRequest = ::exitApplication,
        title = "Blocks World",
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp
        )
    ) {
        MaterialTheme {
            BlocksWorldApp(appState)
        }
    }
}
