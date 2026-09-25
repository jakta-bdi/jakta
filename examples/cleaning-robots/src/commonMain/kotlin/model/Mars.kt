package model

import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * A cell of the grid.
 *
 * @property x the column.
 * @property y the row.
 */
data class Pos(val x: Int, val y: Int)

/**
 * A snapshot of the planet: robot r1 checks every slot for garbage, robot r2 burns it.
 *
 * @property size the number of rows and columns.
 * @property r1 where the collecting robot is.
 * @property r2 where the burning robot is.
 * @property garbage the cells holding garbage.
 * @property r1Carrying whether r1 is carrying garbage.
 */
data class MarsState(
    val size: Int,
    val r1: Pos = Pos(0, 0),
    val r2: Pos = Pos(size / 2, size / 2),
    val garbage: Set<Pos> = emptySet(),
    val r1Carrying: Boolean = false,
) {
    /**
     * Whether r1 is on the last slot, having checked all the others.
     */
    val r1OnLastSlot: Boolean get() = r1 == Pos(size - 1, size - 1)
}

/**
 * The planet, changed by the robots' actions and by the user dropping garbage.
 *
 * @param initial the initial state.
 * @param random decides whether picking up garbage succeeds.
 * @param pickSuccess the probability that picking up garbage succeeds, as the robot's arm is unreliable.
 */
class Mars(initial: MarsState, private val random: Random = Random.Default, private val pickSuccess: Double = 0.5) {
    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the planet.
     */
    val state: StateFlow<MarsState> = mutableState.asStateFlow()

    /**
     * Moves r1 to the next slot, row by row.
     */
    fun next(): MarsState = mutableState.updateAndGet { mars ->
        val next = if (mars.r1.x < mars.size - 1) Pos(mars.r1.x + 1, mars.r1.y) else Pos(0, mars.r1.y + 1)
        mars.copy(r1 = next.takeIf { it.y < mars.size } ?: mars.r1)
    }

    /**
     * Moves r1 one step towards [target], first along columns and then along rows.
     */
    fun moveTowards(target: Pos): MarsState = mutableState.updateAndGet { mars ->
        val (x, y) = mars.r1
        val step = when {
            x != target.x -> Pos(x + (target.x - x).coerceIn(-1, 1), y)
            else -> Pos(x, y + (target.y - y).coerceIn(-1, 1))
        }
        mars.copy(r1 = step)
    }

    /**
     * Tries to pick up the garbage where r1 is; it may fail, leaving the garbage there.
     */
    fun pick(): MarsState = mutableState.updateAndGet { mars ->
        if (mars.r1 in mars.garbage && !mars.r1Carrying && random.nextDouble() < pickSuccess) {
            mars.copy(garbage = mars.garbage - mars.r1, r1Carrying = true)
        } else {
            mars
        }
    }

    /**
     * Drops what r1 carries where it is.
     */
    fun drop(): MarsState = mutableState.updateAndGet { mars ->
        if (mars.r1Carrying) mars.copy(garbage = mars.garbage + mars.r1, r1Carrying = false) else mars
    }

    /**
     * Burns the garbage where r2 is.
     */
    fun burn(): MarsState = mutableState.updateAndGet { mars -> mars.copy(garbage = mars.garbage - mars.r2) }

    /**
     * Adds or removes garbage at [pos], as the user clicks on the grid.
     */
    fun toggleGarbage(pos: Pos) {
        mutableState.update { mars ->
            mars.copy(garbage = if (pos in mars.garbage) mars.garbage - pos else mars.garbage + pos)
        }
    }
}

/**
 * A planet of [size]×[size] cells with some [count] of garbage scattered at random.
 */
fun randomMars(size: Int, count: Int, random: Random = Random.Default): MarsState {
    val cells = (0 until size).flatMap { x -> (0 until size).map { y -> Pos(x, y) } } - Pos(size / 2, size / 2)
    return MarsState(size, garbage = cells.shuffled(random).take(count).toSet())
}
