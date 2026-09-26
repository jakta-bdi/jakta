package it.unibo.jakta.dsl

import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun <T> String.ifGoalMatch(goal: String, returnValue: T): T? = if (this == goal) returnValue else null

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

// TODO we should probably remove this utility function as it hides some stuff...
fun <Body : Any> executeInTestScope(node: TestScope.() -> ExecutableNode<Body>) {
    runTest {
        val job = launch {
            CoroutineNodeRunner<Body, ExecutableNode<Body>>(SharedMemoryNetwork()).run(node())
        }
        job.join()
    }
}

/**
 * Runs the node until it terminates. Unlike executeInTestScope, it lets tests return runTest,
 * so that assertions made after the run are actually awaited on JS.
 */
suspend fun <Body : Any> ExecutableNode<Body>.runToEnd() =
    CoroutineNodeRunner<Body, ExecutableNode<Body>>(SharedMemoryNetwork()).run(this)
