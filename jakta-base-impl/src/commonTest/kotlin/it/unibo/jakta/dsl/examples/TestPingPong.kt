package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.BodyWithName
import it.unibo.jakta.dsl.MessagingSkill
import it.unibo.jakta.dsl.MessagingSkillImpl
import it.unibo.jakta.dsl.NodeTerminationSkill
import it.unibo.jakta.dsl.NodeTerminationSkillImpl
import it.unibo.jakta.dsl.SimpleMessage
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.node.Node
import kotlin.test.Test

class TestPingPong {

    interface CustomSkillSet : NodeTerminationSkill, MessagingSkill
    class CustomSkillSetImpl(node: Node<BodyWithName, CustomSkillSet>) :
        CustomSkillSet,
        NodeTerminationSkill by NodeTerminationSkillImpl(node),
        MessagingSkill by MessagingSkillImpl(node)

    private fun <Goal : Any> LocalNodeBuilder<CustomSkillSet, BodyWithName>.messageEnabledAgent(
        name: String,
        block: AgentBuilder<SimpleMessage, Goal, CustomSkillSet, BodyWithName>.() -> Unit,
    ) {
        agent(name) {
            embodiedAs { BodyWithName(name) }
            withSkills { CustomSkillSetImpl(it) }
            handlesMessageEvents { BeliefAddEvent(it) }
            block()
        }
    }

    val node = node {
        messageEnabledAgent("Bob") {
            hasPlans {
                adding.belief {
                    this.takeIf { it == SimpleMessage("Message", "Alice") }
                } triggers {
                        agent.print(context.toString())
                        with(skills) {
                            agent.sendMessage("Hello Back!", context.sender)
                        }
                }
            }
        }

        messageEnabledAgent("Alice") {
            hasInitialGoals {
                !"sendMessage"
            }
            hasPlans {
                adding.goal {
                    ifGoalMatch("sendMessage")
                } triggers {
                    agent.print("Hello World!")
                    with(skills) {
                        agent.sendMessage("Message", "Bob")
                    }
                }
                adding.belief {
                    this.takeIf { it == SimpleMessage("Hello Back!", "Bob") }
                } triggers {
                    agent.print("Terminating!")
                    skills.terminateNode()
                }
            }
        }
    }

    @Test
    fun testPingPong() {
        Logger.setMinSeverity(Severity.Error)
        executeInTestScope { node }
    }
}
