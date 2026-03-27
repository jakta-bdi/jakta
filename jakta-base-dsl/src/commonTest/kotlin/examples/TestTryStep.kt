package examples

import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import it.unibo.jakta.node
import it.unibo.jakta.node.ManualStepNodeRunner
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.triggers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class TestTryStep {

    var firstStep = false;
    var secondStep = false;
    var done = false;

    val helloWorld: Node<Any, NodeTerminationSkillImpl> = node {
        agent("Hello world agent") {
            body = object {}
            withSkills { NodeTerminationSkillImpl(it) }
            believes {
                +"testBelief"
            }
            hasPlans {
                adding.belief {
                    this.takeIf { it == "testBelief" }
                } triggers {
                    agent.print("Belief added: $context")
                    agent.print("First step");
                    firstStep = true;
                    delay(1000);
                    agent.print("Second step");
                    secondStep = true;
                    delay(1000)
                    agent.print("Third step, done!");
                    done = true;
                    skills.terminateNode()
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
            val runner = ManualStepNodeRunner<Any, NodeTerminationSkillImpl, Node<Any, NodeTerminationSkillImpl>>()
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
