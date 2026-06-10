package ui


import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun BlocksWorldScreen(vm: BlocksWorldViewModel) {

    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            vm.refresh()
            kotlinx.coroutines.delay(16.milliseconds) // ~30 FPS (or 16 for 60 FPS)
        }
    }

    Column {
        BlocksWorldPlane(state)
    }
}
