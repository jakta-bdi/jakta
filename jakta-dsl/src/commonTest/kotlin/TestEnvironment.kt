import it.unibo.jakta.environment.Environment
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestCoroutineScheduler

class TestEnvironment(val seed: Int = 1234) : Environment {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun currentTime(): Long = currentCoroutineContext()[TestCoroutineScheduler]?.currentTime
        ?: super.currentTime()

    override suspend fun nextRandom(): Double = Random(seed).nextDouble()

    fun test() {
        // Just a test function to illustrate usage
    }
}
