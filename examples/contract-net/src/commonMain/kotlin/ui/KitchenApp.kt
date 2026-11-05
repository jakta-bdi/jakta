package ui

import Kitchen
import KitchenState
import WALK_TIME
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unibo.jakta.contract_net.generated.resources.Res
import it.unibo.jakta.contract_net.generated.resources.noto_emoji_dishes
import kotlin.math.roundToInt
import kotlin.time.Duration
import model.Dish
import model.LoggedMessage
import model.Order
import model.OrderStatus
import model.Station
import org.jetbrains.compose.resources.Font

private const val MIN_SPEED = 0.5f
private const val MAX_SPEED = 3f
private const val TENTHS = 10.0

/** The colours the chefs are drawn with, one per chef. */
@Suppress("MagicNumber")
private val CHEF_COLOURS = listOf(Color(0xFF3F51B5), Color(0xFFC62828), Color(0xFF2E7D32), Color(0xFFF9A825))

/**
 * The main Composable function for the Kitchen application.
 */
@Composable
fun KitchenApp(app: KitchenAppState) {
    val scope = rememberCoroutineScope()
    val orders by app.restaurant.orders.collectAsState()
    val messages by app.restaurant.messages.collectAsState()
    val kitchens = app.kitchens.map { it.state.collectAsState().value }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app.isRunning) {
                OutlinedButton(onClick = { app.reset() }) { Text("Reset") }
            } else {
                Button(onClick = { app.start(scope) }) { Text("Open the restaurant") }
            }
            Text(
                "Served: ${orders.count { it.status is OrderStatus.Served }} of ${orders.size}",
                modifier = Modifier.width(130.dp),
            )
            Text("Chefs: ${app.chefCount}")
            Slider(
                value = app.chefCount.toFloat(),
                onValueChange = { if (it.roundToInt() != app.chefCount) app.changeChefCount(it.roundToInt()) },
                valueRange = 1f..MAX_CHEFS.toFloat(),
                steps = MAX_CHEFS - 2,
                enabled = !app.isRunning,
                modifier = Modifier.width(100.dp),
            )
            Text("Speed: ${(app.speed * TENTHS).roundToInt() / TENTHS}x")
            Slider(
                value = app.speed.toFloat(),
                onValueChange = { app.speed = it.toDouble() },
                valueRange = MIN_SPEED..MAX_SPEED,
                modifier = Modifier.width(120.dp),
            )
        }
        Text(
            "Order dishes, click a station to switch it on or off, and send a chef to nap: asleep, it is offline.",
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Divider(modifier = Modifier.padding(top = 8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            OrdersPanel(orders, kitchens, onOrder = app::order, modifier = Modifier.width(200.dp).fillMaxHeight())
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                app.kitchens.chunked(2).forEachIndexed { row, pair ->
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEachIndexed { column, kitchen ->
                            val index = row * 2 + column
                            KitchenCard(
                                kitchen,
                                kitchens[index],
                                CHEF_COLOURS[index],
                                walkMillis = app.restaurant.realTime(WALK_TIME).inWholeMilliseconds,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (app.kitchens.size <= 2) Spacer(modifier = Modifier.weight(1f))
            }
            Column(modifier = Modifier.width(340.dp).fillMaxHeight()) {
                MessageLogPanel(messages, modifier = Modifier.weight(1f).fillMaxWidth())
                AgentTracePanel(modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun OrdersPanel(
    orders: List<Order>,
    kitchens: List<KitchenState>,
    onOrder: (Dish) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(orders.size) {
        if (orders.isNotEmpty()) listState.animateScrollToItem(orders.lastIndex)
    }
    Column(modifier = modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Order a dish", style = MaterialTheme.typography.subtitle2)
        Dish.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pair.forEach { dish ->
                    OutlinedButton(
                        onClick = { onOrder(dish) },
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DishEmoji(dish, 16.sp)
                                Text(" ${dish.label()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                "${dish.station.shortLabel()} · ${dish.cookingTime.inWholeSeconds} s",
                                fontSize = 10.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                }
            }
        }
        Divider()
        Text("Orders", style = MaterialTheme.typography.subtitle2)
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(orders, key = { it.id }) { order ->
                val cook = kitchens.firstOrNull { it.cooking?.id == order.id }
                OrderSlip(order, cooking = cook != null && order.status is OrderStatus.Assigned)
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun OrderSlip(order: Order, cooking: Boolean) {
    val (text, colour) = when (val status = order.status) {
        OrderStatus.New -> "waiting for the waiter" to Color(0xFF9E9E9E)
        OrderStatus.Asking -> "asking the chefs..." to Color(0xFF1565C0)
        OrderStatus.Retrying -> "nobody can cook it: asking again soon" to Color(0xFFE65100)
        is OrderStatus.Assigned if cooking -> "${status.chef} is cooking it" to Color(0xFF6A1B9A)
        is OrderStatus.Assigned -> "${status.chef}, ready in ${status.readyIn.format()}" to Color(0xFF00897B)
        is OrderStatus.Served -> "served by ${status.chef}" to Color(0xFF2E7D32)
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(colour.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .border(1.dp, colour, RoundedCornerShape(4.dp)).padding(6.dp),
    ) {
        Row(modifier = Modifier.width(96.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("#${order.id} ", fontWeight = FontWeight.Bold)
            DishEmoji(order.dish, 14.sp)
            Text(" ${order.dish.label()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Text(text, color = colour, fontSize = 13.sp)
    }
}

@Suppress("MagicNumber")
@Composable
private fun KitchenCard(kitchen: Kitchen, state: KitchenState, colour: Color, walkMillis: Long, modifier: Modifier) {
    Column(
        modifier = modifier.border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(6.dp)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(colour, RoundedCornerShape(6.dp)))
            Text(
                " ${kitchen.chef.replaceFirstChar { it.uppercase() }}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
            )
            Text(
                "  served ${state.served}",
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = kitchen::toggleNap) { Text(if (state.napping) "Wake up" else "Nap") }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            KitchenFloor(state, colour, walkMillis, onStation = kitchen::toggleStation)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Rail:", fontSize = 13.sp)
            if (state.rail.isEmpty()) Text("no tickets", fontSize = 13.sp, color = Color.Gray)
            state.rail.forEach { ticket ->
                val current = ticket.id == state.cooking?.id
                Row(
                    modifier = Modifier.background(Color(0xFFFFF9C4), RoundedCornerShape(2.dp))
                        .border(1.dp, Color(0xFFFBC02D), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "#${ticket.id} ",
                        fontSize = 12.sp,
                        fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                    )
                    DishEmoji(ticket.dish, 12.sp)
                }
            }
        }
    }
}

/** Where a station sits on the floor, as fractions of its width and height: top-left corner. */
@Suppress("MagicNumber")
private fun Station.place(): Offset = when (this) {
    Station.CHOPPING_BOARD -> Offset(0.22f, 0.04f)
    Station.STOVE -> Offset(0.6f, 0.04f)
    Station.GRILL -> Offset(0.22f, 0.74f)
    Station.OVEN -> Offset(0.6f, 0.74f)
}

private const val STATION_WIDTH = 0.28f
private const val STATION_HEIGHT = 0.22f

/** Where the chef stands to work at [station], or at the pass if `null`: the centre of the chef. */
@Suppress("MagicNumber")
private fun standingSpot(station: Station?): Offset = when (station) {
    null -> Offset(0.17f, 0.5f)
    else -> station.place().let { Offset(it.x + STATION_WIDTH / 2, if (it.y < 0.5f) 0.4f else 0.6f) }
}

/**
 * The kitchen seen from above: the floor, the stations along the walls, the pass on the left, and the chef walking
 * between them.
 */
@Suppress("MagicNumber", "LongMethod")
@Composable
private fun KitchenFloor(state: KitchenState, colour: Color, walkMillis: Long, onStation: (Station) -> Unit) {
    BoxWithConstraints(modifier = Modifier.aspectRatio(1.45f)) {
        val width = maxWidth
        val height = maxHeight
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            // floor tiles and walls
            val tile = size.height / 8
            var y = 0f
            var row = 0
            while (y < size.height) {
                var x = 0f
                var column = row
                while (x < size.width) {
                    drawRect(
                        if (column % 2 == 0) Color(0xFFEFEBE3) else Color(0xFFE3DED3),
                        topLeft = Offset(x, y),
                        size = Size(tile, tile),
                    )
                    x += tile
                    column++
                }
                y += tile
                row++
            }
            drawRect(Color(0xFF5D4037), style = Stroke(width = 6f))
        }
        // the pass, where dishes are served
        Box(
            modifier = Modifier.offset(0.dp, height * 0.3f).size(width * 0.08f, height * 0.4f)
                .background(Color(0xFFB0BEC5), RoundedCornerShape(3.dp)).border(1.dp, Color(0xFF78909C)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "P\nA\nS\nS",
                fontSize = 8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F),
            )
        }
        Station.entries.forEach { station ->
            val place = station.place()
            StationTile(
                station,
                present = station in state.stations,
                progress = state.progress.takeIf { state.cooking?.dish?.station == station },
                modifier = Modifier.offset(width * place.x, height * place.y)
                    .size(width * STATION_WIDTH, height * STATION_HEIGHT)
                    .clickable { onStation(station) },
            )
        }
        val spot = standingSpot(state.chefAt)
        val chefSize = minOf(width, height) * 0.2f
        val x by animateDpAsState(width * spot.x - chefSize / 2, tween(walkMillis.toInt(), easing = LinearEasing))
        val y by animateDpAsState(height * spot.y - chefSize / 2, tween(walkMillis.toInt(), easing = LinearEasing))
        Chef(colour, chefSize, state.napping, state.cooking?.dish, modifier = Modifier.offset(x, y))
    }
}

@Suppress("MagicNumber", "LongMethod")
@Composable
private fun StationTile(station: Station, present: Boolean, progress: Float?, modifier: Modifier) {
    val base = when (station) {
        Station.CHOPPING_BOARD -> Color(0xFFA1887F)
        Station.STOVE -> Color(0xFF78909C)
        Station.GRILL -> Color(0xFF616161)
        Station.OVEN -> Color(0xFFBF5B3C)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(if (present) 1f else 0.35f)) {
            val corner = CornerRadius(6f, 6f)
            if (!present) {
                drawRoundRect(Color(0xFFBDBDBD), cornerRadius = corner)
                drawRoundRect(Color(0xFF757575), cornerRadius = corner, style = Stroke(width = 2f))
                return@Canvas
            }
            drawRoundRect(base, cornerRadius = corner)
            val w = size.width
            val h = size.height
            when (station) {
                Station.CHOPPING_BOARD -> {
                    drawRoundRect(
                        Color(0xFFD7B98E),
                        topLeft = Offset(w * 0.15f, h * 0.2f),
                        size = Size(w * 0.7f, h * 0.6f),
                        cornerRadius = corner,
                    )
                    drawLine(
                        Color(0xFF9E9E9E),
                        Offset(w * 0.6f, h * 0.35f),
                        Offset(w * 0.8f, h * 0.7f),
                        strokeWidth = 4f,
                    )
                }

                Station.STOVE -> listOf(0.3f, 0.7f).forEach { cx ->
                    drawCircle(Color(0xFF263238), radius = h * 0.25f, center = Offset(w * cx, h * 0.5f))
                    drawCircle(
                        if (progress != null) Color(0xFFFF7043) else Color(0xFF90A4AE),
                        radius = h * 0.18f,
                        center = Offset(w * cx, h * 0.5f),
                        style = Stroke(width = 3f),
                    )
                }

                Station.GRILL -> for (i in 1..5) {
                    val x = w * i / 6f
                    drawLine(
                        if (progress != null) Color(0xFFFF8A65) else Color(0xFF9E9E9E),
                        Offset(x, h * 0.15f),
                        Offset(x, h * 0.85f),
                        strokeWidth = 3f,
                    )
                }

                Station.OVEN -> drawRoundRect(
                    if (progress != null) Color(0xFFFFB74D) else Color(0xFF3E2723),
                    topLeft = Offset(w * 0.2f, h * 0.2f),
                    size = Size(w * 0.6f, h * 0.6f),
                    cornerRadius = corner,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (station == Station.CHOPPING_BOARD) "Board" else station.label(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = if (present) FontStyle.Normal else FontStyle.Italic,
                color = if (present) Color.White else Color(0xFF616161),
                modifier = Modifier.background(Color(0x66000000).takeIf { present } ?: Color.Transparent)
                    .padding(horizontal = 3.dp),
            )
        }
        if (progress != null) {
            LinearProgressIndicator(
                progress = progress,
                color = Color(0xFFFFEB3B),
                backgroundColor = Color(0x66000000),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(5.dp),
            )
        }
    }
}

/** The chef seen from above: shoulders in the chef's colour, a white hat, and the [dish] it cooks in a bubble. */
@Suppress("MagicNumber")
@Composable
private fun Chef(colour: Color, size: Dp, napping: Boolean, dish: Dish?, modifier: Modifier) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2
            drawCircle(Color(0x33000000), radius = r, center = center + Offset(2f, 3f))
            drawCircle(colour, radius = r)
            drawCircle(Color.White, radius = r * 0.62f)
            drawCircle(Color(0xFFE0E0E0), radius = r * 0.62f, style = Stroke(width = 2f))
            drawCircle(Color(0xFFEEEEEE), radius = r * 0.35f)
        }
        if (napping) Snoring(modifier = Modifier.offset(size * 0.6f, -size * 0.7f))
        if (dish != null) {
            Box(
                modifier = Modifier.offset(size * 0.55f, -size * 0.45f).size(size * 0.75f)
                    .background(Color.White, RoundedCornerShape(50))
                    .border(1.dp, colour, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                DishEmoji(dish, (size.value * 0.42f).sp)
            }
        }
    }
}

/** A "z Z z" floating up and fading, over and over. */
@Suppress("MagicNumber")
@Composable
private fun Snoring(modifier: Modifier) {
    val transition = rememberInfiniteTransition()
    val rise by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
    )
    Box(modifier = modifier.offset(y = (-14 * rise).dp).alpha(1f - rise * 0.8f)) {
        Text("z Z z", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5C6BC0))
    }
}

/**
 * Shows the messages the agents exchanged, coloured by performative, following the newest one.
 */
@Suppress("MagicNumber")
@Composable
private fun MessageLogPanel(messages: List<LoggedMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    Column(modifier = modifier.background(Color(0xFFFAFAFA)).padding(8.dp)) {
        Text("Messages", style = MaterialTheme.typography.subtitle2)
        LazyColumn(state = listState) {
            items(messages) { message ->
                Row {
                    Text(
                        "${message.sender} -> ${message.receivers.joinToString()}: ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                    )
                    Text(
                        message.content,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (message.lost) Color(0xFFBDBDBD) else performativeColour(message.performative),
                        textDecoration = if (message.lost) TextDecoration.LineThrough else null,
                    )
                    if (message.lost) {
                        Text(" (asleep)", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
private fun performativeColour(performative: String): Color = when (performative) {
    "cfp" -> Color(0xFF1565C0)
    "propose" -> Color(0xFF2E7D32)
    "refuse" -> Color(0xFFE65100)
    "accept_proposal" -> Color(0xFF00897B)
    "reject_proposal" -> Color(0xFF757575)
    "done" -> Color(0xFF6A1B9A)
    "failure" -> Color(0xFFC62828)
    "cancel" -> Color(0xFFAD1457)
    else -> Color(0xFF424242)
}

private fun Enum<*>.label(): String = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun Station.shortLabel(): String = if (this == Station.CHOPPING_BOARD) "board" else name.lowercase()

/** The emoji of the dish. */
private val Dish.emoji: String get() = when (this) {
    Dish.SALAD -> "🥗"
    Dish.STEAK -> "🥩"
    Dish.BURGER -> "🍔"
    Dish.SOUP -> "🍲"
    Dish.PIZZA -> "🍕"
    Dish.CAKE -> "🍰"
}

/**
 * The emoji of [dish], drawn with the dish emoji of Noto Color Emoji bundled with the example, so that it looks the
 * same on every platform and in every browser.
 */
@Composable
private fun DishEmoji(dish: Dish, fontSize: TextUnit) =
    Text(dish.emoji, fontFamily = FontFamily(Font(Res.font.noto_emoji_dishes)), fontSize = fontSize)

@Suppress("MagicNumber")
private fun Duration.format(): String = "${(inWholeMilliseconds / 100) / TENTHS} s"
