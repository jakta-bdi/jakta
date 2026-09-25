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
import model.Mars
import model.MarsState
import model.Pos

/**
 * What the robots perceive: the whole planet.
 *
 * @property mars the current state of the planet.
 */
data class MarsPerception(val mars: MarsState) : Perception

/**
 * The actions robots can perform on Mars; each one takes some time, then shows the new state to every robot.
 *
 * @param mars the planet.
 * @param node the node of the robots, to publish perceptions.
 * @param stepTime how long an action takes.
 */
class MarsEnvironment(private val mars: Mars, private val node: Node<*>, private val stepTime: () -> Duration) {

    /**
     * Shows the planet to every robot.
     */
    fun look() = node.publishEvent(MarsPerception(mars.state.value))

    private suspend fun act(action: () -> Unit) {
        delay(stepTime())
        action()
        look()
    }

    /**
     * Moves r1 to the next slot.
     */
    suspend fun next() = act { mars.next() }

    /**
     * Moves r1 one step towards column [x] and row [y].
     */
    suspend fun moveTowards(x: Int, y: Int) = act { mars.moveTowards(Pos(x, y)) }

    /**
     * Makes r1 try to pick up garbage.
     */
    suspend fun pick() = act { mars.pick() }

    /**
     * Makes r1 drop the garbage it carries.
     */
    suspend fun drop() = act { mars.drop() }

    /**
     * Makes r2 burn the garbage where it is.
     */
    suspend fun burn() = act { mars.burn() }
}

private val perceivedQueries = listOf(
    newContextBeliefQuery { "pos"(Atom.of("r1"), X, Y) },
    newContextBeliefQuery { "pos"(Atom.of("r2"), X, Y) },
    newContextBeliefQuery { "garbage"(X) },
    newContextBeliefQuery { Atom.of("last_slot") },
)

/**
 * Updates the perceived beliefs `pos(R, X, Y)`, `garbage(R)` (garbage where robot R is) and `last_slot`
 * (r1 is on the last slot); other beliefs, like r1's note `pos(last, X, Y)`, are left alone.
 */
fun handleMarsPerception(event: MarsPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val old = beliefs.filter { belief -> perceivedQueries.any { belief.matchBelief(it) != null } }.toSet()
    val new = event.mars.toBeliefs()
    return AgentUpdate.Belief(new - old, old - new)
}

private fun MarsState.toBeliefs(): Set<PrologBelief> {
    fun fact(struct: Struct): PrologBelief = Fact.of(struct)
    fun pos(robot: String, pos: Pos) = fact(Struct.of("pos", Atom.of(robot), Integer.of(pos.x), Integer.of(pos.y)))
    return buildSet {
        add(pos("r1", r1))
        add(pos("r2", r2))
        if (r1 in garbage) add(fact(Struct.of("garbage", Atom.of("r1"))))
        if (r2 in garbage) add(fact(Struct.of("garbage", Atom.of("r2"))))
        if (r1OnLastSlot) add(fact(Atom.of("last_slot")))
    }
}
