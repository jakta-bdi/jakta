package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.AgentTerminationSkill
import it.unibo.jakta.skills.BaseAgentTerminationSkill
import it.unibo.jakta.skills.BaseNodeTerminationSkill
import it.unibo.jakta.skills.NodeTerminationSkill
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

class SkillSet(val node: Node<*, *>) :
    NodeTerminationSkill by BaseNodeTerminationSkill(node),
    AgentTerminationSkill by BaseAgentTerminationSkill(node)

class TestMultipleAgentDelays {
    val helloWorld =
        node {
            agent {
                embodiedAs { object {} }
                withSkills { SkillSet(it) }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello...")
                        delay(10000)
                        agent.print("...World!")
                        skills.terminateNode()
                    }
                }
            }
            agent {
                embodiedAs { object {} }
                withSkills { SkillSet(it) }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("I will be faster...")
                        delay(5.seconds)
                        agent.print("...than you!")
                        with(skills) {
                            agent.terminate()
                        }
                    }
                }
            }
        }

    @Test
    fun testMultipleAgentsDelays() {
        Logger.setMinSeverity(Severity.Error)
        executeInTestScope { helloWorld }
    }
}
