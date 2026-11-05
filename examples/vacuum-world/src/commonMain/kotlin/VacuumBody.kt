import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.Content
import model.Direction
import model.Pos
import model.Square
import model.VacuumWorld

/** How long the robot takes to turn left or right. */
private val TIME_TO_TURN = 0.5.seconds

/** How long the robot takes to move one cell. */
private val TIME_TO_MOVE = 1.5.seconds

/** How long the robot takes to clean. */
private val TIME_TO_CLEAN = 3.seconds

/**
 * The state of the robot itself.
 *
 * @property position the cell where the robot is.
 * @property facing where the robot faces.
 * @property cleaned how many dusty cells the robot cleaned.
 */
data class RobotState(val position: Pos, val facing: Direction, val cleaned: Int = 0)

/**
 * The body of a vacuum robot, placed in the world it cleans. It owns the robot's state, carries out its actions,
 * which take time on the world's clock and never overlap, and its sensors read what is around the robot.
 *
 * @param world the world the robot cleans.
 * @param start where the robot starts.
 * @param facing where the robot faces at the start.
 */
class VacuumBody(private val world: VacuumWorld, start: Pos, facing: Direction) {
    private val mutableState = MutableStateFlow(RobotState(start, facing))
    private val busy = Mutex()

    init {
        check(world.enter(start)) { "The robot cannot start at $start" }
    }

    /**
     * The current state of the robot.
     */
    val state: StateFlow<RobotState> = mutableState.asStateFlow()

    /** Turns left, and returns when the robot faces its new direction. */
    suspend fun turnLeft() = turn { it.left }

    /** Turns right, and returns when the robot faces its new direction. */
    suspend fun turnRight() = turn { it.right }

    /**
     * Moves one cell forward, and returns when the robot arrived.
     * An obstacle or another robot in the way makes the robot stay where it is.
     */
    suspend fun forward() = busy.withLock {
        val from = state.value.position
        val ahead = Square.FORWARD.cellFrom(from, state.value.facing)
        val free = world.enter(ahead)
        var arrived = false
        try {
            world.passTime(TIME_TO_MOVE)
            arrived = free
        } finally {
            // stopped halfway, the robot stays where it was
            if (free) world.leave(if (arrived) from else ahead)
            mutableState.update { it.copy(position = if (arrived) ahead else from) }
        }
    }

    /** Cleans the cell of the robot, and returns when it is done. */
    suspend fun clean() = busy.withLock {
        world.passTime(TIME_TO_CLEAN)
        val removed = world.removeDust(state.value.position)
        mutableState.update { it.copy(cleaned = it.cleaned + if (removed) 1 else 0) }
    }

    /** Reads the sensors. */
    fun sense(): VacuumPerception {
        val robot = state.value
        val around = world.state.value

        // the robot does not see itself: its own cell is only dusty or empty
        fun seen(square: Square): Content = square.cellFrom(robot.position, robot.facing).let { cell ->
            when {
                square != Square.HERE -> around.contentAt(cell)
                cell in around.dust -> Content.DUST
                else -> Content.EMPTY
            }
        }
        return VacuumPerception(robot.position, robot.facing, Square.entries.associateWith(::seen))
    }

    private suspend fun turn(towards: (Direction) -> Direction) = busy.withLock {
        world.passTime(TIME_TO_TURN)
        mutableState.update { it.copy(facing = towards(it.facing)) }
    }
}
