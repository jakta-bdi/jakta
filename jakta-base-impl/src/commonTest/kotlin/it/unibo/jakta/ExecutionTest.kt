package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentState
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.LocalNode
import it.unibo.jakta.plan.GoalAdditionPlan
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest

class ExecutionTest {

    class MyPrint(val node: Node<*, *>) {
        suspend fun prettyPrint(value: String) = println("${currentCoroutineContext()} - $value")
        fun stop() {
            node.terminateNode()
        }
    }

    @Test
    fun testAgentExecution() {
        val node = LocalNode<Any, MyPrint>()
        val node2 = LocalNode<Any, MyPrint>()

        fun agentSpecGenerator(
            agentname: String,
            node: LocalNode<*, MyPrint>,
        ): Set<AgentSpecification<String, String, MyPrint, Any>> = setOf(
            object : AgentSpecification<String, String, MyPrint, Any> {
                override val body = object {}
                override val initialState: AgentState<String, String, MyPrint> = BaseAgentState(
                    beliefs = listOf(),
                    intentions = setOf(),
                    beliefPlans = listOf(),
                    goalPlans = listOf(
                        GoalAdditionPlan(
                            trigger = { it == "hello" },
                            guard = { it },
                            body = {
                                it.skills.prettyPrint("PLKUTO")
                                it.skills.stop()
                            },
                            resultType = typeOf<Unit>(),
                        ),
                    ),
                    perceptionHandler = { null },
                    messageHandler = { null },
                    skills = MyPrint(node),
                )
                override val initialGoals: List<String> = listOf("hello")
                override val id: AgentID = BaseAgentID(agentname)
            },
        )

        val runner = CoroutineNodeRunner<Any, MyPrint, LocalNode<Any, MyPrint>>()

        runTest {
            agentSpecGenerator("Agent1", node).forEach { node.addAgent(it) }
            runner.run(node)

            agentSpecGenerator("Agent2", node2).forEach { node2.addAgent(it) }
            runner.run(node2)
        }
    }
}
