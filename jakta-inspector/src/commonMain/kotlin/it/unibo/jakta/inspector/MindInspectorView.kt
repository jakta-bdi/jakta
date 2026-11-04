@file:Suppress("MagicNumber") // colours and sizes of the view

package it.unibo.jakta.inspector

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unibo.jakta.agent.AgentID
import kotlin.time.Duration

/**
 * Shows what the agents seen by the [inspectors] have in mind, and the messages they exchange: one tab for each
 * inspector, when there are more.
 *
 * Pick an agent on the left to see its beliefs, intentions and plans after the event selected in its history:
 * the view follows the newest event until you step back, and the interaction flow then stops at that moment too.
 * Click a message to see the mind of the agent that received it, when it did.
 */
@Composable
fun MindInspectorView(vararg inspectors: MindInspector, modifier: Modifier = Modifier) {
    require(inspectors.isNotEmpty()) { "The mind inspector view needs an inspector to show" }
    var tab by remember { mutableStateOf(0) }
    Column(modifier.fillMaxSize().background(Color.White)) {
        if (inspectors.size > 1) {
            TabRow(tab, backgroundColor = Color.White) {
                inspectors.forEachIndexed { index, inspector ->
                    Tab(tab == index, { tab = index }, text = { Text(inspector.name) })
                }
            }
        }
        val inspector = inspectors[tab.coerceAtMost(inspectors.lastIndex)]
        key(inspector) { RunView(inspector, Modifier.weight(1f)) }
    }
}

// a request to show the mind of [agent] when it received a message sent at [time], or when it sent one nobody got
private class Jump(val agent: AgentID, val time: Duration, val received: Boolean)

@Composable
private fun RunView(inspector: MindInspector, modifier: Modifier) {
    val agents by inspector.agents.collectAsState()
    val messages by inspector.messages.collectAsState()
    var selectedId by remember { mutableStateOf<AgentID?>(null) }
    val selected = agents.find { it.id == selectedId } ?: agents.firstOrNull()
    var until by remember { mutableStateOf<Duration?>(null) }
    var onlySelected by remember { mutableStateOf(false) }
    var jump by remember { mutableStateOf<Jump?>(null) }

    Column(modifier.fillMaxSize()) {
        Row(Modifier.weight(0.6f).fillMaxWidth()) {
            AgentList(agents, selected, { selectedId = it.id }, Modifier.width(200.dp).fillMaxHeight())
            Divider(Modifier.fillMaxHeight().width(1.dp))
            if (selected == null) {
                Text("No agent yet", Modifier.padding(16.dp), color = Color.Gray)
            } else {
                MindPanel(selected, jump, onMoment = { until = it }, modifier = Modifier.weight(1f))
            }
        }
        Divider()
        val moment = until
        val arrows = messages
            .filter { moment == null || it.time <= moment }
            .flatMap { message -> message.receivers.ifEmpty { listOf(null) }.map { Arrow(message, it) } }
            .filter { !onlySelected || selected == null || selected.id == it.from || selected.id == it.to }
        Column(Modifier.weight(0.4f).fillMaxWidth().padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Title(if (moment == null) "Interaction flow" else "Interaction flow until ${moment.label()}")
                Spacer(Modifier.weight(1f))
                Checkbox(onlySelected, { onlySelected = it })
                Text("only ${selected?.id?.displayName ?: "the selected agent"}'s", fontSize = 12.sp)
            }
            val lanes = (agents.groupBy { it.node }.values.flatten().map { it.id } + messages.map { it.message.sender })
                .distinct()
            InteractionFlow(lanes, arrows, selected?.id) { arrow ->
                val agent = arrow.to ?: arrow.from
                selectedId = agent
                jump = Jump(agent, arrow.message.time, received = arrow.to != null)
            }
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
private fun MindPanel(agent: InspectedAgent, jump: Jump?, onMoment: (Duration?) -> Unit, modifier: Modifier) {
    val history by agent.history.collectAsState()
    var showSteps by remember { mutableStateOf(false) }
    // null follows the newest snapshot
    var cursor by remember(agent, showSteps) { mutableStateOf<Int?>(null) }
    val visible = if (showSteps) history else history.filter { it.kind != EventKind.STEP }
    val index = cursor?.coerceIn(0, visible.lastIndex) ?: visible.lastIndex
    val snapshot = visible.getOrNull(index)
    LaunchedEffect(cursor, snapshot) { onMoment(if (cursor == null) null else snapshot?.time) }
    LaunchedEffect(jump) {
        if (jump?.agent != agent.id) return@LaunchedEffect
        val target = if (jump.received) {
            visible.indexOfFirst { it.time >= jump.time && it.kind == EventKind.MESSAGE }
        } else {
            visible.indexOfLast { it.time <= jump.time }
        }
        if (target >= 0) cursor = target
    }

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

// one arrow of the interaction flow: a message to one of its receivers, or to nobody yet
private class Arrow(val message: InspectedMessage, val to: AgentID?) {
    val from: AgentID get() = message.message.sender
}

private val laneWidth = 150.dp
private val timeWidth = 80.dp
private val rowHeight = 30.dp

/**
 * A sequence diagram of the [arrows], one lifeline for each of the [lanes]: a message nobody handled (yet) ends in a
 * cross, a message to oneself loops back.
 */
@Composable
private fun InteractionFlow(lanes: List<AgentID>, arrows: List<Arrow>, selected: AgentID?, onClick: (Arrow) -> Unit) {
    val listState = rememberLazyListState()
    FollowIndex(listState, arrows.lastIndex)
    val width = timeWidth + laneWidth * lanes.size
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row(Modifier.width(width)) {
            Spacer(Modifier.width(timeWidth))
            lanes.forEach { id ->
                Text(
                    id.displayName,
                    Modifier.width(laneWidth),
                    textAlign = TextAlign.Center,
                    fontWeight = if (id == selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        LazyColumn(Modifier.width(width).weight(1f), state = listState) {
            items(arrows) { arrow -> ArrowRow(lanes, arrow) { onClick(arrow) } }
        }
    }
}

@Composable
private fun ArrowRow(lanes: List<AgentID>, arrow: Arrow, onClick: () -> Unit) {
    val from = lanes.indexOf(arrow.from)
    val to = arrow.to?.let(lanes::indexOf)?.takeIf { it >= 0 }
    val lost = arrow.to == null
    val colour = kindColour(arrow.message.kind).let { if (lost) it.copy(alpha = 0.5f) else it }
    val payload = arrow.message.message.payload.toString()
    val label = if (payload.startsWith(
            arrow.message.kind,
            ignoreCase = true,
        )
    ) {
        payload
    } else {
        "${arrow.message.kind}: $payload"
    }
    // the label sits above the arrow, between the lifelines it joins (or next to the sender's)
    val left = minOf(from, to ?: from)
    val span = if (to == null || to == from) 1 else maxOf(from, to) - left
    Row(Modifier.height(rowHeight).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Text(arrow.message.time.label(), Modifier.width(timeWidth), fontSize = 11.sp, color = Color.Gray)
        Box(Modifier.width(laneWidth * lanes.size).fillMaxHeight()) {
            Canvas(Modifier.matchParentSize()) { drawArrow(lanes.size, from, to, lost, colour) }
            Text(
                label,
                Modifier.offset(
                    x = laneWidth * left + laneWidth / 2,
                ).width(laneWidth * span).padding(horizontal = 6.dp),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = colour,
                textAlign = TextAlign.Center,
                textDecoration = if (lost) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun DrawScope.drawArrow(lanes: Int, from: Int, to: Int?, lost: Boolean, colour: Color) {
    val lane = laneWidth.toPx()
    fun centre(index: Int) = lane * index + lane / 2
    repeat(lanes) { drawLine(Color(0xFFE0E0E0), Offset(centre(it), 0f), Offset(centre(it), size.height), 2f) }
    val y = size.height * 0.75f
    val start = Offset(centre(from), y)
    val head = 7.dp.toPx()
    when {
        lost -> {
            val end = Offset(start.x + lane * 0.6f, y)
            drawLine(colour, start, end, 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
            drawLine(colour, end + Offset(-head / 2, -head / 2), end + Offset(head / 2, head / 2), 2f)
            drawLine(colour, end + Offset(-head / 2, head / 2), end + Offset(head / 2, -head / 2), 2f)
        }

        to == null || to == from -> {
            // to oneself: out to the right and back
            val out = lane * 0.3f
            val top = Offset(start.x, y - head)
            drawLine(colour, top, top + Offset(out, 0f), 2f)
            drawLine(colour, top + Offset(out, 0f), start + Offset(out, 0f), 2f)
            drawLine(colour, start + Offset(out, 0f), start, 2f)
            drawHead(start, -1f, head, colour)
        }

        else -> {
            val end = Offset(centre(to), y)
            drawLine(colour, start, end, 2f)
            drawHead(end, if (to > from) 1f else -1f, head, colour)
        }
    }
}

// an arrow head at [tip], pointing right when [direction] is 1, left when -1
private fun DrawScope.drawHead(tip: Offset, direction: Float, size: Float, colour: Color) {
    drawLine(colour, tip, tip + Offset(-direction * size, -size / 2), 2f)
    drawLine(colour, tip, tip + Offset(-direction * size, size / 2), 2f)
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
