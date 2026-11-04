package model

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Where a robot starts.
 *
 * @property position the cell where it starts.
 * @property facing where it faces at the start.
 */
data class Start(val position: Pos, val facing: Direction)

/**
 * Where up to four robots start on the default map, one per corner, each facing into the map.
 */
val ROBOT_STARTS = listOf(
    Start(Pos(0, 0), Direction.EAST),
    Start(Pos(11, 7), Direction.WEST),
    Start(Pos(11, 0), Direction.WEST),
    Start(Pos(0, 7), Direction.EAST),
)

/**
 * A snapshot of the world. The robots' own state lives in their bodies: the world only knows which cells they
 * take, to keep them apart.
 *
 * @property width the number of columns.
 * @property height the number of rows.
 * @property obstacles the walls.
 * @property dust the dusty cells.
 * @property robots the cells taken by robots: a moving robot takes both the cell it leaves and the next one.
 * @property time how many seconds of the world's clock passed.
 */
data class WorldState(
    val width: Int,
    val height: Int,
    val obstacles: Set<Pos>,
    val dust: Set<Pos> = emptySet(),
    val robots: Set<Pos> = emptySet(),
    val time: Int = 0,
) {
    /**
     * Whether [pos] is an obstacle: a wall, or out of the map.
     */
    fun isObstacle(pos: Pos): Boolean = pos.x !in 0 until width || pos.y !in 0 until height || pos in obstacles

    /**
     * What [pos] holds.
     */
    fun contentAt(pos: Pos): Content = when {
        isObstacle(pos) -> Content.OBSTACLE
        pos in robots -> Content.ROBOT
        pos in dust -> Content.DUST
        else -> Content.EMPTY
    }

    /**
     * The cells with no obstacle, dust or robot.
     */
    val emptyCells: List<Pos> get() = (0 until height).flatMap { y ->
        (0 until width).map { x -> Pos(x, y) }
    }.filter { contentAt(it) == Content.EMPTY }
}

/**
 * The world the robots clean: it runs on its own clock, and dust appears by itself or where the user clicks,
 * whatever the robots do.
 *
 * @param initial the initial state.
 * @param speed how fast the world's clock runs: 1 is real time, 2 twice as fast.
 * @param dustChance the probability that an empty cell gets new dust each second.
 * @param random where new dust appears.
 */
class VacuumWorld(
    initial: WorldState,
    private val speed: () -> Double = { 1.0 },
    private val dustChance: () -> Double = { 0.0 },
    private val random: Random = Random.Default,
) {
    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the world.
     */
    val state: StateFlow<WorldState> = mutableState.asStateFlow()

    /**
     * Returns once [time] passed on the world's clock.
     */
    suspend fun passTime(time: Duration) = delay(time / speed())

    /**
     * Runs the world's clock until cancelled: every second, each empty cell gets new dust with [dustChance].
     */
    suspend fun simulate() {
        while (true) {
            passTime(1.seconds)
            val chance = dustChance()
            mutableState.update { world ->
                val newDust = world.emptyCells.filter { random.nextDouble() < chance }
                world.copy(time = world.time + 1, dust = world.dust + newDust)
            }
        }
    }

    /**
     * Lets a robot take [pos], returning whether it could: not if it is an obstacle or another robot is there.
     */
    fun enter(pos: Pos): Boolean {
        var entered = false
        mutableState.update { world ->
            entered = !world.isObstacle(pos) && pos !in world.robots
            if (entered) world.copy(robots = world.robots + pos) else world
        }
        return entered
    }

    /**
     * Frees [pos], as a robot leaves it.
     */
    fun leave(pos: Pos) = mutableState.update { it.copy(robots = it.robots - pos) }

    /**
     * Removes the dust at [pos], returning whether there was any.
     */
    fun removeDust(pos: Pos): Boolean {
        var removed = false
        mutableState.update { world ->
            removed = pos in world.dust
            world.copy(dust = world.dust - pos)
        }
        return removed
    }

    /**
     * Adds or removes dust at [pos], as the user clicks on the map.
     */
    fun toggleDust(pos: Pos) = mutableState.update { world ->
        when {
            world.isObstacle(pos) -> world
            pos in world.dust -> world.copy(dust = world.dust - pos)
            else -> world.copy(dust = world.dust + pos)
        }
    }
}

private val defaultLayout = listOf(
    "............",
    "..##....#...",
    "..#.....#...",
    "........#...",
    "....###.....",
    "..........#.",
    ".#........#.",
    "............",
)

/**
 * The default map, with [dustCount] dusty cells scattered at random, away from where the robots start.
 */
fun defaultMap(dustCount: Int = 8, random: Random = Random.Default): WorldState {
    val obstacles = defaultLayout.flatMapIndexed { y, row ->
        row.indices.filter { row[it] == '#' }.map { Pos(it, y) }
    }
    val empty = WorldState(width = defaultLayout[0].length, height = defaultLayout.size, obstacles = obstacles.toSet())
    val dust = (empty.emptyCells - ROBOT_STARTS.map { it.position }.toSet()).shuffled(random).take(dustCount)
    return empty.copy(dust = dust.toSet())
}
