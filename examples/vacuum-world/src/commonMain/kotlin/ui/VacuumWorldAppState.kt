package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import model.Pos
import model.VacuumWorld
import model.defaultWorld
import vacuumNode

private const val DEFAULT_STEP_TIME_MS = 250
private const val DEFAULT_DUST_CHANCE = 0.05

/**
 * State holder for the Vacuum World application: the world and the robot's run.
 *
 * @property agentDispatcher where the robot runs, off the UI thread on desktop.
 */
class VacuumWorldAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        AgentTrace.install()
    }

    /**
     * The world the robot cleans.
     */
    var world by mutableStateOf(VacuumWorld(defaultWorld()))
        private set

    /**
     * How long each robot action takes.
     */
    var stepTime: Duration by mutableStateOf(DEFAULT_STEP_TIME_MS.milliseconds)

    /**
     * The probability that new dust appears after each robot action.
     */
    var dustChance by mutableStateOf(DEFAULT_DUST_CHANCE)

    /**
     * Whether the robot is working.
     */
    var isRunning by mutableStateOf(false)
        private set

    private var job: Job? = null

    /**
     * Stops the robot and restores the map with some fresh dust.
     */
    fun reset() {
        stop()
        world = VacuumWorld(defaultWorld())
    }

    /**
     * Adds or removes dust, also while the robot works.
     */
    fun toggleDust(pos: Pos) = world.toggleDust(pos)

    /**
     * Stops the robot where it is.
     */
    fun stop() {
        job?.cancel()
        job = null
        isRunning = false
    }

    /**
     * Starts the robot, which keeps cleaning until stopped; it remembers nothing of previous runs.
     */
    fun start(scope: CoroutineScope) {
        if (isRunning) return
        val currentWorld = world
        AgentTrace.clear()
        isRunning = true
        val newJob = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                mas(NodeBuilders.baseNode()) {
                    vacuumNode(currentWorld, stepTime = { stepTime }, dustChance = { dustChance })
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            } finally {
                // a stopped run must not flag a newer one as finished
                if (job === coroutineContext.job) isRunning = false
            }
        }
        job = newJob
        newJob.start()
    }
}
