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
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import terminateNode

class SkillSet(val node: Node<*>) :
    NodeTerminationSkill by NodeTerminationSkillImpl(node),
    AgentTerminationSkill by AgentTerminationSkillImpl(node)

class TestMultipleAgentDelays {
    val helloWorld =
        node {
            context(SkillSet(node)) {
                agent {
                    embodiedAs { object {} }
                    hasInitialGoals {
                        !"goal"
                    }
                    hasPlans {
                        adding.goal {
                            ifGoalMatch("goal")
                        } triggers {
                            agent.print("Hello...")
                            delay(1.seconds)
                            agent.print("...World!")
                            terminateNode()
                        }
                    }
                }
                agent {
                    embodiedAs { object {} }
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
                            with(contextOf<AgentTerminationSkill>()) {
                                agent.terminate()
                            }
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
