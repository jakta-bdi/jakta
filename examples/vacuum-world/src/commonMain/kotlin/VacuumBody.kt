import it.unibo.jakta.agent.Agent
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.belief.newContextBeliefQuery
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.Skill
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import model.Content
import model.Direction
import model.Pos
import model.ROBOT_START
import model.Square
import model.VacuumWorld

/**
 * The state of the robot itself.
 *
 * @property position the cell where the robot is.
 * @property facing where the robot faces.
 * @property cleaned how many dusty cells the robot cleaned.
 */
data class RobotState(val position: Pos, val facing: Direction, val cleaned: Int = 0) {
    /**
     * The cell of [square], relative to the robot.
     */
    fun cellOf(square: Square): Pos = when (square) {
        Square.HERE -> position
        Square.FORWARD -> position + facing
        Square.LEFT -> position + facing.left
        Square.RIGHT -> position + facing.right
    }
}

/**
 * What the robot's sensors read: where it is, where it faces, the time, and what the four squares around it hold.
 *
 * @property location the cell where the robot is.
 * @property facing where the robot faces.
 * @property time the current time.
 * @property squares what each square relative to the robot holds.
 */
data class VacuumPerception(
    val location: Pos,
    val facing: Direction,
    val time: Int,
    val squares: Map<Square, Content>,
) : Perception

/**
 * The body of a vacuum cleaner, placed in the [world] it cleans: it owns the robot's state, moves it, and senses
 * what is around it.
 *
 * @param start where the robot starts, facing east.
 */
class VacuumBody(val world: VacuumWorld, start: Pos = ROBOT_START) {
    private val mutableState = MutableStateFlow(RobotState(start, Direction.EAST))

    /**
     * The current state of the robot.
     */
    val state: StateFlow<RobotState> = mutableState.asStateFlow()

    /** Moves one cell forward, unless something is in the way. */
    fun forward() = mutableState.update { robot ->
        val ahead = robot.cellOf(Square.FORWARD)
        if (world.state.value.contentAt(ahead) == Content.OBSTACLE) robot else robot.copy(position = ahead)
    }

    /** Turns left. */
    fun turnLeft() = mutableState.update { it.copy(facing = it.facing.left) }

    /** Turns right. */
    fun turnRight() = mutableState.update { it.copy(facing = it.facing.right) }

    /** Cleans the cell where the robot is. */
    fun clean() {
        if (world.removeDust(state.value.position)) mutableState.update { it.copy(cleaned = it.cleaned + 1) }
    }

    /** Reads the sensors. */
    fun sense(): VacuumPerception {
        val robot = state.value
        val world = world.state.value
        val squares = Square.entries.associateWith { world.contentAt(robot.cellOf(it)) }
        return VacuumPerception(robot.position, robot.facing, world.time, squares)
    }
}

/**
 * The robot's actions, performed through the body of the acting agent; each one takes some time, lets time pass in
 * the world, may let new dust appear, and is followed by what the robot senses, delivered only to its body.
 *
 * @param node the node of the robot, to find its body and publish perceptions.
 * @param stepTime how long an action takes.
 * @param dustChance the probability that new dust appears after each action.
 */
class VacuumSkill(node: Node<VacuumBody>, private val stepTime: () -> Duration, private val dustChance: () -> Double) :
    Skill<VacuumBody>(node) {
    private val Agent.body get() = node.agents.getValue(id)

    /**
     * Lets the robot sense its surroundings.
     */
    fun Agent.look() {
        val body = body
        node.publishEvent(body.sense()) { it === body }
    }

    private suspend fun Agent.act(action: VacuumBody.() -> Unit) {
        delay(stepTime())
        body.action()
        body.world.tick()
        body.world.maybeSpawnDust(dustChance())
        look()
    }

    /** Moves forward, unless something is in the way. */
    suspend fun Agent.forward() = act(VacuumBody::forward)

    /** Turns left. */
    suspend fun Agent.turnLeft() = act(VacuumBody::turnLeft)

    /** Turns right. */
    suspend fun Agent.turnRight() = act(VacuumBody::turnRight)

    /** Cleans the cell of the robot. */
    suspend fun Agent.clean() = act(VacuumBody::clean)
}

private val perceivedQueries = listOf(
    newContextBeliefQuery { "location"(X, Y) },
    newContextBeliefQuery { "direction"(X) },
    newContextBeliefQuery { "time"(X) },
    newContextBeliefQuery { "square"(X, Y) },
)

/**
 * Updates the perceived beliefs, as in the EIS Vacuum World: `location(X, Y)`, `direction(D)`, `time(T)`,
 * and `square(S, C)` for S in here, forward, left, right, and C in obstacle, dust, empty.
 * It also keeps the robot's memory of the dust it saw, `dust_at(X, Y)`: added when a square shows dust, removed when
 * it shows none. Other beliefs, like the robot's memory of `visited(X, Y, T)`, are left alone.
 */
fun handleVacuumPerception(event: VacuumPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val old = beliefs.filter { belief -> perceivedQueries.any { belief.matchBelief(it) != null } }.toSet()
    val new = event.toBeliefs()
    val robot = RobotState(event.location, event.facing)
    val (dusty, clean) = event.squares.entries.partition { it.value == Content.DUST }
    val seenDust = dusty.map { dustAt(robot.cellOf(it.key)) }.toSet()
    val goneDust = clean.map { dustAt(robot.cellOf(it.key)) }.filter { it in beliefs }.toSet()
    return AgentUpdate.Belief(new - old + (seenDust - beliefs.toSet()), old - new + goneDust)
}

private fun dustAt(cell: Pos): PrologBelief = Fact.of(Struct.of("dust_at", Integer.of(cell.x), Integer.of(cell.y)))

private fun VacuumPerception.toBeliefs(): Set<PrologBelief> {
    fun atom(value: Enum<*>) = Atom.of(value.name.lowercase())
    return buildSet {
        add(Fact.of(Struct.of("location", Integer.of(location.x), Integer.of(location.y))))
        add(Fact.of(Struct.of("direction", atom(facing))))
        add(Fact.of(Struct.of("time", Integer.of(time))))
        for ((square, content) in squares) {
            add(Fact.of(Struct.of("square", atom(square), atom(content))))
        }
    }
}
