import it.unibo.jakta.environment.baseImpl.AbstractEnvironment

open class TestEnvironment(val seed: Int = 1234) : AbstractEnvironment() {

//    @OptIn(ExperimentalCoroutinesApi::class)
//    override suspend fun currentTime(): Long = currentCoroutineContext()[TestCoroutineScheduler]?.currentTime
//        ?: super.currentTime()
//
//    override suspend fun nextRandom(): Double = Random(seed).nextDouble()

    fun test() {
        // Just a test function to illustrate usage
    }
}
