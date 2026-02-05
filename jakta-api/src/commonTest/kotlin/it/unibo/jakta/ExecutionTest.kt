package it.unibo.jakta

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.basImpl.BaseAgentID
import it.unibo.jakta.agent.basImpl.BaseAgentState
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.environment.Runtime
import it.unibo.jakta.environment.Topology
import it.unibo.jakta.environment.baseImpl.BaseRuntime
import it.unibo.jakta.node.baseImpl.BaseNode
import it.unibo.jakta.plan.baseImpl.GoalAdditionPlan
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ExecutionTest {

    object CustomTopology : Topology<Int, Int> {
        override val defaultPosition: Int = 0

        override fun isValid(position: Int): Boolean  = true

        override fun distance(from: Int, to: Int): Double = 0.0

        override fun move(from: Int, displacement: Int): Int = from + displacement
    }

    class MyPrint(val runtime: Runtime<*, *, AgentBody>) {
        fun prettyPrint(value: String) = println(value)
        fun stop() {
            runtime.terminateMAS()
        }
    }

    @Test
    fun testAgentExecution() {

        val runtime = BaseRuntime<Int, Int, AgentBody>(CustomTopology)



        val agentsSpec: Map<AgentSpecification<*, *, *, AgentBody>, Int> = mapOf(
            object : AgentSpecification<String, String, MyPrint, AgentBody> {
                override val body: AgentBody = object: AgentBody {}
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
                            resultType = typeOf<Unit>()
                        )
                    ),
                    perceptionHandler = { null } ,
                    messageHandler = { null },
                    skills = MyPrint(runtime),
                )
                override val initialGoals: List<String> = listOf("hello")
                override val id: AgentID = BaseAgentID("MI PIACE")
            } to 0
        )

        val node = BaseNode(runtime, agentsSpec)

        runTest {
            node.run()
        }
    }
}
