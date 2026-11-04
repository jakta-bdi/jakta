@file:Suppress("MagicNumber") // colours and sizes of the view

package it.unibo.jakta.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unibo.jakta.agent.AgentID
import kotlin.time.Duration

/**
 * Shows what the agents seen by [inspector] have in mind, and the messages they exchange.
 *
 * Pick an agent on the left to see its beliefs, intentions and plans after the event selected in its history:
 * the view follows the newest event until you step back, and the message log then stops at that moment too.
 */
@Composable
fun MindInspectorView(inspector: MindInspector, modifier: Modifier = Modifier) {
    val agents by inspector.agents.collectAsState()
    val messages by inspector.messages.collectAsState()
    var selectedId by remember { mutableStateOf<AgentID?>(null) }
    val selected = agents.find { it.id == selectedId } ?: agents.firstOrNull()
    var until by remember { mutableStateOf<Duration?>(null) }
    var onlySelected by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().background(Color.White)) {
        Row(Modifier.weight(0.65f).fillMaxWidth()) {
            AgentList(agents, selected, { selectedId = it.id }, Modifier.width(200.dp).fillMaxHeight())
            Divider(Modifier.fillMaxHeight().width(1.dp))
            if (selected == null) {
                Text("No agent yet", Modifier.padding(16.dp), color = Color.Gray)
            } else {
                MindPanel(selected, onMoment = { until = it }, modifier = Modifier.weight(1f))
            }
        }
        Divider()
        val moment = until
        val shown = messages.filter { message ->
            val involved = selected == null || selected.id == message.message.sender || selected.id in message.receivers
            (moment == null || message.time <= moment) && (!onlySelected || involved)
        }
        Column(Modifier.weight(0.35f).fillMaxWidth().padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Title(if (moment == null) "Messages" else "Messages until ${moment.label()}")
                Spacer(Modifier.weight(1f))
                Checkbox(onlySelected, { onlySelected = it })
                Text("only ${selected?.id?.displayName ?: "the selected agent"}'s", fontSize = 12.sp)
            }
            MessageLog(shown)
        }
    }
}

@Composable
private fun AgentList(
    agents: List<InspectedAgent>,
    selected: InspectedAgent?,
    onSelect: (InspectedAgent) -> Unit,
    modifier: Modifier,
) {
    val nodes = agents.map { it.node }.distinct()
    LazyColumn(modifier.padding(8.dp)) {
        nodes.forEachIndexed { index, node ->
            item { Title(if (node == null) "Unknown node" else "Node ${index + 1}") }
            items(agents.filter { it.node == node }) { agent ->
                val events = agent.history.collectAsState().value.size
                Text(
                    "${agent.id.displayName} ($events)",
                    Modifier.fillMaxWidth()
                        .background(if (agent == selected) Color(0xFFE3F2FD) else Color.Transparent)
                        .clickable { onSelect(agent) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = if (agent == selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun MindPanel(agent: InspectedAgent, onMoment: (Duration?) -> Unit, modifier: Modifier) {
    val history by agent.history.collectAsState()
    var showSteps by remember { mutableStateOf(false) }
    // null follows the newest snapshot
    var cursor by remember(agent, showSteps) { mutableStateOf<Int?>(null) }
    val visible = if (showSteps) history else history.filter { it.kind != EventKind.STEP }
    val index = cursor?.coerceIn(0, visible.lastIndex) ?: visible.lastIndex
    val snapshot = visible.getOrNull(index)
    LaunchedEffect(cursor, snapshot) { onMoment(if (cursor == null) null else snapshot?.time) }

    Column(modifier.fillMaxHeight().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(agent.id.displayName, style = MaterialTheme.typography.h6)
            Spacer(Modifier.width(12.dp))
            StepButton("|<", visible.isNotEmpty()) { cursor = 0 }
            StepButton("<", index > 0) { cursor = index - 1 }
            StepButton(">", index < visible.lastIndex) { cursor = index + 1 }
            StepButton(">|", cursor != null) { cursor = null }
            Text(
                if (snapshot == null) {
                    ""
                } else {
                    "event ${index + 1} of ${visible.size}, at ${snapshot.time.label()}" +
                        if (cursor == null) " (live)" else ""
                },
                fontSize = 12.sp,
            )
            Spacer(Modifier.weight(1f))
            Checkbox(showSteps, { showSteps = it })
            Text("intention steps", fontSize = 12.sp)
        }
        if (snapshot == null) return@Column
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxHeight().padding(end = 8.dp)) {
                Title("Event")
                Mono(snapshot.event, kindColour(snapshot.kind), bold = true)
                Title("Beliefs (${snapshot.beliefs.size})")
                LazyColumn(Modifier.weight(1f)) { items(snapshot.beliefs) { Mono(it.toString()) } }
                Title("Intentions (${snapshot.intentions.size})")
                LazyColumn(Modifier.weight(1f)) {
                    items(snapshot.intentions) { intention ->
                        Mono("#${intention.number}", Color.Gray)
                        intention.goals.forEachIndexed { depth, goal ->
                            val current = depth == intention.goals.lastIndex
                            val colour = if (current) Color(0xFF1565C0) else Color.Unspecified
                            Mono("  ".repeat(depth + 1) + goal, colour, bold = current)
                        }
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Title("History")
                val listState = rememberLazyListState()
                FollowIndex(listState, index)
                LazyColumn(Modifier.weight(2f), state = listState) {
                    itemsIndexed(visible) { i, step ->
                        Row(
                            Modifier.fillMaxWidth()
                                .background(if (i == index) Color(0xFFE3F2FD) else Color.Transparent)
                                .clickable { cursor = i },
                        ) {
                            Mono(step.time.label().padStart(10) + "  ", Color.Gray)
                            Mono(step.event, kindColour(step.kind))
                        }
                    }
                }
                Title("Plans (${snapshot.plans.size})")
                LazyColumn(Modifier.weight(1f)) { itemsIndexed(snapshot.plans) { i, plan -> Mono("${i + 1}. $plan") } }
            }
        }
    }
}

@Composable
private fun MessageLog(messages: List<InspectedMessage>) {
    val listState = rememberLazyListState()
    FollowIndex(listState, messages.lastIndex)
    LazyColumn(state = listState) {
        items(messages) { message ->
            val lost = message.receivers.isEmpty()
            Row {
                Mono(message.time.label().padStart(10) + "  ", Color.Gray)
                Mono(
                    "${message.message.sender.displayName} -> " +
                        (if (lost) "nobody yet" else message.receivers.joinToString { it.displayName }) + ": ",
                    Color(0xFF616161),
                )
                Text(
                    "[${message.kind}] ${message.message.payload}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lost) Color(0xFFBDBDBD) else kindColour(message.kind),
                    textDecoration = if (lost) TextDecoration.LineThrough else null,
                )
            }
        }
    }
}

@Composable
private fun FollowIndex(listState: LazyListState, index: Int) {
    LaunchedEffect(index) { if (index >= 0) listState.animateScrollToItem(index) }
}

@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick, enabled = enabled, modifier = Modifier.width(48.dp)) { Text(label, fontSize = 12.sp) }
}

@Composable
private fun Title(text: String) {
    Text(text, Modifier.padding(top = 6.dp, bottom = 2.dp), style = MaterialTheme.typography.subtitle2)
}

@Composable
private fun Mono(text: String, colour: Color = Color.Unspecified, bold: Boolean = false) {
    Text(
        text,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = colour,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    )
}

private fun kindColour(kind: EventKind): Color = when (kind) {
    EventKind.CREATED -> Color(0xFF424242)
    EventKind.BELIEF_ADDED -> Color(0xFF2E7D32)
    EventKind.BELIEF_REMOVED -> Color(0xFFE65100)
    EventKind.GOAL_ADDED -> Color(0xFF1565C0)
    EventKind.GOAL_REMOVED -> Color(0xFF757575)
    EventKind.GOAL_FAILED -> Color(0xFFC62828)
    EventKind.STEP -> Color(0xFF9E9E9E)
    EventKind.MESSAGE -> Color(0xFF6A1B9A)
    EventKind.PERCEPTION -> Color(0xFF00897B)
}

private val messageColours = listOf(
    Color(0xFF1565C0),
    Color(0xFF2E7D32),
    Color(0xFFE65100),
    Color(0xFF00897B),
    Color(0xFF6A1B9A),
    Color(0xFFC62828),
    Color(0xFFAD1457),
    Color(0xFF4E342E),
)

// the same kind always gets the same colour: String.hashCode is the same on every platform
private fun kindColour(kind: String): Color = messageColours[kind.hashCode().mod(messageColours.size)]

private fun Duration.label(): String {
    val millis = inWholeMilliseconds
    return "${millis / 1000}.${(millis % 1000).toString().padStart(3, '0')} s"
}
