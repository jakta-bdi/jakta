import it.unibo.jakta.node.NodeRunner
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun <T> String.ifGoalMatch(goal: String, returnValue: T): T? = if (this == goal) returnValue else null

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

fun executeInTestScope(nodeRunner: TestScope.() -> NodeRunner<*, *, *>) {
    runTest {
        val job = launch {
            nodeRunner().run()
        }
        job.join()
    }
}
