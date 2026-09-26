package model

import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A cell of the grid; (0, 0) is the top-left corner.
 *
 * @property x the column.
 * @property y the row.
 */
data class Pos(val x: Int, val y: Int) {
    /**
     * The next cell towards [direction].
     */
    operator fun plus(direction: Direction) = Pos(x + direction.dx, y + direction.dy)
}

/**
 * Where the robot can face.
 *
 * @property dx the column step when moving forward.
 * @property dy the row step when moving forward.
 */
enum class Direction(val dx: Int, val dy: Int) {
    /** Up. */
    NORTH(0, -1),

    /** Right. */
    EAST(1, 0),

    /** Down. */
    SOUTH(0, 1),

    /** Left. */
    WEST(-1, 0),
    ;

    /** The direction after turning left. */
    val left: Direction get() = entries[(ordinal + entries.size - 1) % entries.size]

    /** The direction after turning right. */
    val right: Direction get() = entries[(ordinal + 1) % entries.size]
}

/**
 * The squares the robot sees, relative to where it faces (as in the EIS Vacuum World).
 */
enum class Square {
    /** Where the robot is. */
    HERE,

    /** In front of the robot. */
    FORWARD,

    /** On the robot's left. */
    LEFT,

    /** On the robot's right. */
    RIGHT,
}

/**
 * What a square holds; outside the map is an obstacle.
 */
enum class Content {
    /** A wall, or the edge of the map. */
    OBSTACLE,

    /** Dust to clean. */
    DUST,

    /** Nothing. */
    EMPTY,
}

/**
 * A snapshot of the world.
 *
 * @property width the number of columns.
 * @property height the number of rows.
 * @property obstacles the walls.
 * @property dust the dusty cells.
 * @property robot where the robot is.
 * @property facing where the robot faces.
 * @property time how many actions the robot performed.
 * @property cleaned how many dusty cells the robot cleaned.
 */
data class VacuumState(
    val width: Int,
    val height: Int,
    val obstacles: Set<Pos>,
    val dust: Set<Pos> = emptySet(),
    val robot: Pos = Pos(0, 0),
    val facing: Direction = Direction.EAST,
    val time: Int = 0,
    val cleaned: Int = 0,
) {
    /**
     * What [pos] holds.
     */
    fun contentAt(pos: Pos): Content = when {
        pos.x !in 0 until width || pos.y !in 0 until height || pos in obstacles -> Content.OBSTACLE
        pos in dust -> Content.DUST
        else -> Content.EMPTY
    }

    /**
     * The cell of [square], relative to the robot.
     */
    fun cellOf(square: Square): Pos = when (square) {
        Square.HERE -> robot
        Square.FORWARD -> robot + facing
        Square.LEFT -> robot + facing.left
        Square.RIGHT -> robot + facing.right
    }

    /**
     * The cells that are neither walls nor dusty.
     */
    val freeCells: List<Pos> get() = (0 until height).flatMap { y ->
        (0 until width).map { x -> Pos(x, y) }
    }.filter { contentAt(it) == Content.EMPTY }
}

/**
 * The world: the robot's actions change it, and dust appears by itself or where the user clicks.
 *
 * @param initial the initial state.
 * @param random where new dust appears.
 */
class VacuumWorld(initial: VacuumState, private val random: Random = Random.Default) {
    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the world.
     */
    val state: StateFlow<VacuumState> = mutableState.asStateFlow()

    private fun act(change: VacuumState.() -> VacuumState) = mutableState.update {
        it.change().copy(time = it.time + 1)
    }

    /**
     * Moves the robot one cell forward, unless something is in the way.
     */
    fun forward() = act { if (contentAt(robot + facing) == Content.OBSTACLE) this else copy(robot = robot + facing) }

    /**
     * Turns the robot left.
     */
    fun turnLeft() = act { copy(facing = facing.left) }

    /**
     * Turns the robot right.
     */
    fun turnRight() = act { copy(facing = facing.right) }

    /**
     * Cleans the cell of the robot.
     */
    fun clean() = act { if (robot in dust) copy(dust = dust - robot, cleaned = cleaned + 1) else this }

    /**
     * With probability [chance], makes dust appear on a random free cell.
     */
    fun maybeSpawnDust(chance: Double) {
        if (random.nextDouble() < chance) {
            mutableState.update { world ->
                world.freeCells.randomOrNull(random)?.let { world.copy(dust = world.dust + it) } ?: world
            }
        }
    }

    /**
     * Adds or removes dust at [pos], as the user clicks on the map.
     */
    fun toggleDust(pos: Pos) = mutableState.update { world ->
        when (world.contentAt(pos)) {
            Content.OBSTACLE -> world
            Content.DUST -> world.copy(dust = world.dust - pos)
            Content.EMPTY -> world.copy(dust = world.dust + pos)
        }
    }
}

private val defaultMap = listOf(
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
 * The default map, with some dust scattered at random.
 */
fun defaultWorld(dustCount: Int = 8, random: Random = Random.Default): VacuumState {
    val obstacles = defaultMap.flatMapIndexed { y, row -> row.indices.filter { row[it] == '#' }.map { Pos(it, y) } }
    val empty = VacuumState(width = defaultMap[0].length, height = defaultMap.size, obstacles = obstacles.toSet())
    return empty.copy(dust = (empty.freeCells - empty.robot).shuffled(random).take(dustCount).toSet())
}
