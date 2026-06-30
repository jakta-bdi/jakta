package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentState
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNode
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
        val node = LocalNode<Any>()
        val node2 = LocalNode<Any>()

        fun agentSpecGenerator(agentname: String, node: LocalNode<*>): Set<AgentSpecification<String, String, Any>> =
            setOf(
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
                                        prettyPrint("PLKUTO")
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
                },
            )

        val runner = CoroutineNodeRunner<Any, LocalNode<Any>>()

        runTest {
            agentSpecGenerator("Agent1", node).forEach { node.addAgent(it) }
            try {
                runner.run(node)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Node execution terminated with exception: ${e.message}")
            }
            agentSpecGenerator("Agent2", node2).forEach { node2.addAgent(it) }
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
