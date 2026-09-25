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
import marsNode
import model.Mars
import model.Pos
import model.randomMars

private const val SIZE = 7
private const val GARBAGE = 6
private const val DEFAULT_STEP_TIME_MS = 300

/**
 * State holder for the Cleaning Robots application: the planet and the robots' run.
 *
 * @property agentDispatcher where the robots run, off the UI thread on desktop.
 */
class CleaningRobotsAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        AgentTrace.install()
    }

    /**
     * The planet the robots clean.
     */
    var mars by mutableStateOf(Mars(randomMars(SIZE, GARBAGE)))
        private set

    /**
     * How long each robot action takes.
     */
    var stepTime: Duration by mutableStateOf(DEFAULT_STEP_TIME_MS.milliseconds)

    /**
     * Whether the robots are working.
     */
    var isRunning by mutableStateOf(false)
        private set

    private var job: Job? = null

    /**
     * Stops the robots, if working, and scatters new garbage.
     */
    fun reset() {
        job?.cancel()
        job = null
        isRunning = false
        mars = Mars(randomMars(SIZE, GARBAGE))
    }

    /**
     * Adds or removes garbage, also while the robots work.
     */
    fun toggleGarbage(pos: Pos) = mars.toggleGarbage(pos)

    /**
     * Sends the robots to work; r1 stops them once it has checked every slot.
     */
    fun start(scope: CoroutineScope) {
        if (isRunning) return
        val currentMars = mars
        AgentTrace.clear()
        isRunning = true
        val newJob = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                mas(NodeBuilders.baseNode()) {
                    marsNode(currentMars) { stepTime }
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            } finally {
                // a cancelled run must not flag a newer one as finished
                if (job === coroutineContext.job) isRunning = false
            }
        }
        job = newJob
        newJob.start()
    }
}
