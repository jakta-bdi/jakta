import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.belief.newContextBeliefQuery
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import kotlin.time.Duration
import kotlinx.coroutines.delay
import model.Square
import model.VacuumState
import model.VacuumWorld

/**
 * What the robot perceives after each action.
 *
 * @property world the current state of the world.
 */
data class VacuumPerception(val world: VacuumState) : Perception

/**
 * The robot's actions; each one takes some time, may let new dust appear, and is followed by a perception.
 *
 * @param world the world.
 * @param node the node of the robot, to publish perceptions.
 * @param stepTime how long an action takes.
 * @param dustChance the probability that new dust appears after each action.
 */
class VacuumEnvironment(
    private val world: VacuumWorld,
    private val node: Node<*>,
    private val stepTime: () -> Duration,
    private val dustChance: () -> Double,
) {
    /**
     * Shows the world to the robot.
     */
    fun look() = node.publishEvent(VacuumPerception(world.state.value))

    private suspend fun act(action: () -> Unit) {
        delay(stepTime())
        action()
        world.maybeSpawnDust(dustChance())
        look()
    }

    /** Moves forward, unless something is in the way. */
    suspend fun forward() = act(world::forward)

    /** Turns left. */
    suspend fun turnLeft() = act(world::turnLeft)

    /** Turns right. */
    suspend fun turnRight() = act(world::turnRight)

    /** Cleans the cell of the robot. */
    suspend fun clean() = act(world::clean)
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
 * Other beliefs, like the robot's memory of `visited(X, Y, T)`, are left alone.
 */
fun handleVacuumPerception(event: VacuumPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val old = beliefs.filter { belief -> perceivedQueries.any { belief.matchBelief(it) != null } }.toSet()
    val new = event.world.toBeliefs()
    return AgentUpdate.Belief(new - old, old - new)
}

private fun VacuumState.toBeliefs(): Set<PrologBelief> {
    fun atom(value: Enum<*>) = Atom.of(value.name.lowercase())
    return buildSet {
        add(Fact.of(Struct.of("location", Integer.of(robot.x), Integer.of(robot.y))))
        add(Fact.of(Struct.of("direction", atom(facing))))
        add(Fact.of(Struct.of("time", Integer.of(time))))
        for (square in Square.entries) {
            add(Fact.of(Struct.of("square", atom(square), atom(contentAt(cellOf(square))))))
        }
    }
}
