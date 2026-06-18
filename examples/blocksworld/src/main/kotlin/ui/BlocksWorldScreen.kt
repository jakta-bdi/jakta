package ui


import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import kotlin.time.Duration.Companion.milliseconds


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
