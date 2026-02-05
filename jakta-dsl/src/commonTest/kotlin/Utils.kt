import it.unibo.jakta.node.Node
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun <T> String.ifGoalMatch(goal: String, returnValue: T): T? = if (this == goal) returnValue else null

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

fun executeInTestScope(node: TestScope.() -> Node<*, *, *>) {
    runTest {
        val job = launch {
            node().run()
        }
        job.join()
    }
}
