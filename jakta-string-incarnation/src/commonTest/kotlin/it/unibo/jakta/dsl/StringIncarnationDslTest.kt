package it.unibo.jakta.dsl.string

import it.unibo.jakta.dsl.mas.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.Node
import kotlin.test.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class StringIncarnationDslTest {
    class TerminationSkill(val node: Node<*, *>) {
        fun terminateNode() {
            node.terminateNode()
        }
    }

	@Test
    fun `node builder builds an agent with string beliefs`() {
        val node = node {
            agent("HelloAgent") {
                embodiedAs { object {} }
                withSkills { TerminationSkill(it) }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello World!")
                        skills.terminateNode()
                    }
                }
            }
        }

        runTest {
            val job = launch {
                CoroutineNodeRunner(setOf(node)).run(node)
            }
            job.join()
        }

	}
}


