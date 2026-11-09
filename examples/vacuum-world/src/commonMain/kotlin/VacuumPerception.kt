import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import model.Content
import model.Direction
import model.Pos
import model.Square

/**
 * What the robot's sensors read: where it is, where it faces, and what the six squares it sees hold.
 *
 * @property position the cell where the robot is.
 * @property facing where the robot faces.
 * @property squares what each square relative to the robot holds.
 */
data class VacuumPerception(val position: Pos, val facing: Direction, val squares: Map<Square, Content>) : Perception

/**
 * Turns a reading of the sensors into beliefs: `position(X, Y)`, `facing(D)` and `square(S, C)` for the six
 * squares S the robot sees, with C in obstacle, robot, dust, empty. Each reading replaces the beliefs of the previous
 * one. It also keeps the robot's memory of the dust it saw, `dust_at(X, Y)`: remembered when a square shows dust,
 * forgotten when it shows the square empty. Other beliefs, like `visited(X, Y, N)`, are left alone.
 */
fun handleVacuumPerception(event: VacuumPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val perceived = event.toBeliefs()
    val previous = beliefs.filter { it.head.functor in perceived.map { new -> new.head.functor } }.toSet()
    val dustSeen = event.cellsWith(Content.DUST).map { cellBelief("dust_at", it) }.toSet() - beliefs.toSet()
    val dustGone = event.cellsWith(Content.EMPTY).map { cellBelief("dust_at", it) }.toSet() intersect beliefs.toSet()
    return AgentUpdate.Belief(
        additions = perceived - previous + dustSeen,
        removals = previous - perceived + dustGone,
    )
}

private fun VacuumPerception.cellsWith(content: Content): List<Pos> =
    squares.filterValues { it == content }.keys.map { it.cellFrom(position, facing) }

private fun VacuumPerception.toBeliefs(): Set<PrologBelief> =
    setOf(cellBelief("position", position), facingBelief(facing)) +
        squares.map { (square, content) -> squareBelief(square, content) }
