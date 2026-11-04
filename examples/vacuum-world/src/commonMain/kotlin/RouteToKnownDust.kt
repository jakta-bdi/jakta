import it.unibo.jakta.dsl.belief.PrologBelief
import model.Content
import model.Direction
import model.Pos

/**
 * The direction of the first step of a shortest path from the robot to the nearest dust it remembers, and that dust's
 * cell, moving only through the cells it knows are free: those it visited and those where it saw dust, except where it
 * sees another robot. Null if it remembers no dust it can reach.
 */
internal fun Collection<PrologBelief>.routeToKnownDust(): Pair<Direction, Pos>? {
    val here = position() ?: return null
    val dust = cells("dust_at") - here
    val robots = squaresWith(Content.ROBOT).map { it.cellFrom(here, facing()) }
    return shortestRoute(from = here, through = cells("visited") + dust - robots.toSet(), to = dust)
}

/**
 * A breadth-first search from [from] to the nearest cell of [to], moving only through the cells of [through]:
 * the direction of the first step of the path, and the cell reached. Null if none can be reached.
 */
fun shortestRoute(from: Pos, through: Set<Pos>, to: Set<Pos>): Pair<Direction, Pos>? {
    // the first step of the path to each cell reached, and the cells to expand, nearest first
    val firstStep = mutableMapOf<Pos, Direction>()
    val frontier = ArrayDeque<Pos>()
    fun reach(cell: Pos, step: Direction) {
        if (cell in through && cell != from && cell !in firstStep) {
            firstStep[cell] = step
            frontier.addLast(cell)
        }
    }
    Direction.entries.forEach { reach(from + it, it) }
    while (frontier.isNotEmpty()) {
        val cell = frontier.removeFirst()
        if (cell in to) return firstStep.getValue(cell) to cell
        Direction.entries.forEach { reach(cell + it, firstStep.getValue(cell)) }
    }
    return null
}
