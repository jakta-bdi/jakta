import it.unibo.jakta.environment.Environment
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestCoroutineScheduler

open class TimeSkill(val seed: Int = 1234) {

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    suspend fun currentTime(): Long = currentCoroutineContext()[TestCoroutineScheduler]?.currentTime
        ?: Clock.System.now().toEpochMilliseconds()

    fun nextRandom(): Double = Random(seed).nextDouble()

    fun test() {
        // Just a test function to illustrate usage
    }
}
