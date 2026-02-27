package examples

import AgentTerminationSkill
import AgentTerminationSkillImpl
import NodeTerminationSkill
import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlinx.coroutines.delay

class SkillSet(val node: it.unibo.jakta.node.Node<*, *>) :
    NodeTerminationSkill by NodeTerminationSkillImpl(node),
    AgentTerminationSkill by AgentTerminationSkillImpl(node)

class TestMultipleAgentDelays {
    val helloWorld =
        node {
            agent {
                body = object {}
                withSkills { SkillSet(this@node.node) }
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
                body = object {}
                withSkills { SkillSet(this@node.node) }
                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("I will be faster...")
                        delay(5000)
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
