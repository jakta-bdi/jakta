package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentState
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNodeConnection
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.GoalAdditionPlan
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest

class ExecutionTest {

    class MyPrint(val node: Node<*>) {
        suspend fun prettyPrint(value: String) = println("${currentCoroutineContext()} - $value")
        fun stop() {
            node.terminateNode()
        }
    }

    @Test
    fun testAgentExecution() {
        val node = BaseNode<Any>()
        val node2 = BaseNode<Any>()

        fun agentSpecGenerator(agentname: String, node: Node<*>): AgentSpecification<String, String, Any> =
            object : AgentSpecification<String, String, Any> {
                override val body = Any()
                override val initialState: AgentState<String, String> = BaseAgentState(
                    beliefs = listOf(),
                    intentions = setOf(),
                    beliefPlans = listOf(),
                    goalPlans = listOf(
                        GoalAdditionPlan(
                            trigger = { it == "hello" },
                            guard = { true },
                            body = {
                                with(MyPrint(node)) {
                                    prettyPrint("PLUTO")
                                    stop()
                                }
                            },
                            resultType = typeOf<Unit>(),
                        ),
                    ),
                    perceptionHandler = { null },
                    messageHandler = { null },
                )
                override val initialGoals: List<String> = listOf("hello")
                override val id: AgentID = BaseAgentID(agentname)
            }

        val runner = CoroutineNodeRunner<Any, BaseNode<Any>>(LocalNodeConnection())

        runTest {
            node.addAgent({ agentSpecGenerator("Agent1", it) })
            try {
                runner.run(node)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Node execution terminated with exception: ${e.message}")
            }
            node2.addAgent({ agentSpecGenerator("Agent2", node2) })
            try {
                runner.run(node2)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Node execution terminated with exception: ${e.message}")
            }
        }
    }
}
