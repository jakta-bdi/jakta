import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith

/**
 * The only thread that talks to Webots, as its controller API is a single control loop.
 * It keeps stepping the simulation, so that the world goes on while agents reason, and calls [onIdle] after each step;
 * it runs the tasks of [run] in between, which step the simulation themselves until their motion ends.
 * If Webots ends the simulation the thread dies, and Webots kills the controller process shortly after.
 */
class Webots {
    private val tasks = LinkedBlockingQueue<YouBot.() -> Unit>()

    /**
     * What to do between tasks, after each step, on the Webots thread; e.g. perceiving the world.
     */
    @Volatile
    var onIdle: YouBot.() -> Unit = {}

    init {
        thread(name = "webots", isDaemon = true) {
            val robot = YouBot()
            while (true) {
                val task = tasks.poll()
                if (task == null) {
                    robot.step()
                    robot.onIdle()
                } else {
                    robot.task()
                }
            }
        }
    }

    /**
     * Runs [block] on the Webots thread and suspends until it ends, returning its result or throwing its exception.
     */
    suspend fun <T> run(block: YouBot.() -> T): T {
        val result = CompletableDeferred<T>()
        tasks.put { result.completeWith(runCatching(block)) }
        return result.await()
    }
}
