package it.unibo.jakta.dsl

import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun <T> String.ifGoalMatch(goal: String, returnValue: T): T? = if (this == goal) returnValue else null

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

// TODO Io questa la rimuoverei e proverei a creare un CoroutineTestRunner
fun <Body : Any> executeInTestScope(node: TestScope.() -> ExecutableNode<Body>) {
    runTest {
        val job = launch {
            CoroutineNodeRunner<Body, ExecutableNode<Body>>().run(node())
        }
        job.join()
    }
}
