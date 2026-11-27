package it.unibo.jakta.dsl.examples

import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestAgentWithoutBody {

    class Robot

    @Test
    fun testAgentWithoutBody(): TestResult {
        var greeted = false
        val greeter = agent<String, String> {
            hasInitialGoals {
                !"greet"
            }
            hasPlanLibrary {
                adding.goal {
                    ifGoalMatch("greet")
                } triggers {
                    greeted = true
                    node.terminateNode()
                }
            }
        }
        return runTest {
            launch {
                CoroutineNodeRunner<Any, ExecutableNode<Any>>(SharedMemoryNetwork())
                    .run(node(NodeBuilders.baseNode()) { withAgents(greeter) })
            }.join()
            assertTrue(greeted)
        }
    }

    @Test
    fun testAgentsWithoutBodyHaveDistinctBodies() {
        // the node tells agents apart by their bodies, e.g. to deliver messages
        val node = node(NodeBuilders.baseNode<Any>()) {}
        val first = agent<String, String> {}(node)
        val second = agent<String, String> {}(node)
        assertNotEquals(first.body, second.body)
    }

    @Test
    fun testAgentsInANodeNeedNoBody(): TestResult {
        var greeted = false
        return runTest {
            launch {
                CoroutineNodeRunner<Any, ExecutableNode<Any>>(SharedMemoryNetwork()).run(
                    node(NodeBuilders.baseNode()) {
                        agent<String, String>(BaseAgentID("greeter")) {
                            hasInitialGoals { !"greet" }
                            hasPlanLibrary {
                                adding.goal { ifGoalMatch("greet") } triggers {
                                    greeted = true
                                    node.terminateNode()
                                }
                            }
                        }
                    },
                )
            }.join()
            assertTrue(greeted)
        }
    }

    @Test
    fun testAgentsOnATypedNodeNeedABody() {
        val error = assertFailsWith<IllegalStateException> {
            node(NodeBuilders.baseNode<Robot>()) {
                agent<String, String> {}
            }
        }
        assertContains(error.message.orEmpty(), "embodiedAs")
    }

    @Test
    fun testEmbodiedAsOverridesTheDefaultBody() {
        val robot = agent<String, String> {
            embodiedAs { Robot() }
        }
        assertTrue(robot(node(NodeBuilders.baseNode<Any>()) {}).body is Robot)
    }
}
