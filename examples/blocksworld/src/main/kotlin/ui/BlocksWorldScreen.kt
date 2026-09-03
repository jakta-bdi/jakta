package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Creates the main screen of the Blocks World application, displaying the current state of the blocks world.
 */
@Composable
fun BlocksWorldScreen(vm: BlocksWorldViewModel) {
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) {
        while (true) {
            vm.refresh()
            kotlinx.coroutines.delay(33.milliseconds)
        }
    }

    Column {
        BlocksWorldPlane(state)
    }
}
