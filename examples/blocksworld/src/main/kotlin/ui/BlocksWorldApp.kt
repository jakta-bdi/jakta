package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun BlocksWorldApp(app: BlocksWorldAppState) {

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // =========================
        // CONTROL PANEL (fixed)
        // =========================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Row {
                OutlinedTextField(
                    value = app.seed,
                    onValueChange = { app.seed = it },
                    label = { Text("Seed") },
                    modifier = Modifier.width(160.dp)
                )

                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = app.blockCount,
                    onValueChange = { app.blockCount = it },
                    label = { Text("Blocks") },
                    modifier = Modifier.width(160.dp)
                )

                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = app.goalText,
                    onValueChange = { app.setGoal(it) },
                    label = { Text("Goal (raw)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            Row {

                Button(onClick = { app.play(scope) }) {
                    Text("Play")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { app.stop() }) {
                    Text("Stop")
                }

                Spacer(Modifier.width(8.dp))

                Button(onClick = { app.reset() }) {
                    Text("Reset")
                }
            }
        }

        Divider()

        // =========================
        // WORLD VIEW (fills space)
        // =========================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            BlocksWorldScreen(app.vm)
        }
    }
}
