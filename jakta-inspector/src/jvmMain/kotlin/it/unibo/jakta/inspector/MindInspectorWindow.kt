package it.unibo.jakta.inspector

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState

/**
 * A desktop window showing the [MindInspectorView] of the [inspectors], to open next to an application's own windows.
 */
@Composable
fun MindInspectorWindow(
    vararg inspectors: MindInspector,
    onCloseRequest: () -> Unit,
    title: String = "JaKtA Mind Inspector",
) {
    Window(onCloseRequest, rememberWindowState(width = 1200.dp, height = 800.dp), title = title) {
        MaterialTheme { MindInspectorView(*inspectors) }
    }
}
