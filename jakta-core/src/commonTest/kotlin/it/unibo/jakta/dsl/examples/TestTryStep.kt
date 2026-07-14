package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.ManualStepNodeRunner
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.ExecutableNode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class TestTryStep {

    var firstStep = false
    var secondStep = false
    var done = false

    val helloWorld: ExecutableNode<Any> = node(NodeBuilders.baseNode()) {
        agent {
            embodiedAs { Any() }
            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                adding.goal {
                    this.takeIf { it == "goal" }
                } triggers {
                    agent.print("First step")
                    firstStep = true
                    delay(1.seconds)
                    agent.print("Second step")
                    secondStep = true
                    delay(1.seconds)
                    agent.print("Third step, done!")
                    done = true
                    node.terminateNode()
                }
            }
        }
    }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Debug)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testBeliefAddition() {
        runTest {
            val runner = ManualStepNodeRunner<Any, ExecutableNode<Any>>()
            runner.run(helloWorld)

            val dispatcher1 = StandardTestDispatcher(testScheduler)

            runner.stepAll(dispatcher1)
            assertFalse(firstStep, "First step should not have been executed yet")

            advanceUntilIdle()

            val dispatcher2 = StandardTestDispatcher(testScheduler)

            runner.stepAll(dispatcher2)
            assertTrue(firstStep, "First step should have been executed")

            advanceUntilIdle()

            val dispatcher3 = StandardTestDispatcher(testScheduler)
            runner.stepAll(dispatcher3)
            assertTrue(secondStep, "Second step should have been executed")

            advanceUntilIdle()

            runner.stepAll(dispatcher3)
            assertTrue(done, "Done should have been executed")
        }
    }
}
