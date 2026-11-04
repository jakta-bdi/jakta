package ui

import VacuumBody
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import model.Pos
import model.ROBOT_STARTS
import model.VacuumWorld
import model.defaultMap
import runVacuumWorld

private const val DEFAULT_ROBOTS = 4
private const val DEFAULT_SPEED = 2.0
private const val DEFAULT_DUST_CHANCE = 0.002

/**
 * The most robots the map holds, one per corner.
 */
val MAX_ROBOTS = ROBOT_STARTS.size

/**
 * State holder for the Vacuum World application: the world, the robots' bodies, and their run.
 *
 * @property agentDispatcher where the robots run, off the UI thread on desktop.
 */
class VacuumWorldAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        AgentTrace.install()
    }

    /**
     * How fast the world and the robots go: 1 is the normal speed.
     */
    var speed by mutableStateOf(DEFAULT_SPEED)

    /**
     * The probability that an empty cell gets new dust each second.
     */
    var dustChance by mutableStateOf(DEFAULT_DUST_CHANCE)

    /**
     * How many robots clean the map; changing it resets the map.
     */
    var robotCount by mutableStateOf(DEFAULT_ROBOTS)
        private set

    /**
     * The world the robots clean.
     */
    var world by mutableStateOf(newWorld())
        private set

    /**
     * The bodies of the robots, placed in the [world].
     */
    var robots by mutableStateOf(placeRobots())
        private set

    /**
     * Whether the robots are working.
     */
    var isRunning by mutableStateOf(false)
        private set

    private var job: Job? = null

    private fun newWorld() = VacuumWorld(defaultMap(), speed = { speed }, dustChance = { dustChance })

    private fun placeRobots(): List<VacuumBody> =
        ROBOT_STARTS.take(robotCount).map { VacuumBody(world, it.position, it.facing) }

    /**
     * Stops the robots and restores the map with some fresh dust.
     */
    fun reset() {
        stop()
        world = newWorld()
        robots = placeRobots()
    }

    /**
     * Sets how many robots clean the map, resetting it.
     */
    fun changeRobotCount(count: Int) {
        robotCount = count.coerceIn(1, MAX_ROBOTS)
        reset()
    }

    /**
     * Adds or removes dust, also while the robots work.
     */
    fun toggleDust(pos: Pos) = world.toggleDust(pos)

    /**
     * Stops the robots where they are.
     */
    fun stop() {
        job?.cancel()
        job = null
        isRunning = false
    }

    /**
     * Starts the robots, which keep cleaning until stopped; they remember nothing of previous runs.
     */
    fun start(scope: CoroutineScope) {
        if (isRunning) return
        val (currentWorld, currentRobots) = world to robots
        AgentTrace.clear()
        isRunning = true
        val newJob = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                runVacuumWorld(currentWorld, currentRobots)
            } finally {
                // a stopped run must not flag a newer one as finished
                if (job === coroutineContext.job) isRunning = false
            }
        }
        job = newJob
        newJob.start()
    }
}
