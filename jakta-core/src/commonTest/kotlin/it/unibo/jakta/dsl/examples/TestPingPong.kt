package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.collections.emptySet
import kotlin.test.Test

class TestPingPong {

    private fun <Goal : Any> LocalNodeBuilder<Any>.messageEnabledAgent(
        id: AgentID,
        block: AgentBuilder<Pair<String, AgentID>, Goal, Any>.() -> Unit,
    ) {
        agent(id) {
            embodiedAs { Any() }
            handlesMessageEvents { message ->
                when (message.payload) {
                    is String -> AgentUpdate.Belief(setOf(Pair(message.payload, message.sender)), emptySet())
                    else -> null
                }
            }
            block()
        }
    }

    val node = node {
        val bob = BaseAgentID("Bob")
        val alice = BaseAgentID("Alice")

        context(MessagingSkill(node)) {
            messageEnabledAgent(bob) {
                hasPlans {
                    adding.belief {
                        this.takeIf { it == Pair("Ping!", alice) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Sending pong to Alice")
                        agent.sendTo(sender, "Pong!")
                    }
                }
            }

            messageEnabledAgent(alice) {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("Sending ping to Bob")
                        agent.sendTo(bob, "Ping!")
                    }
                    adding.belief {
                        this.takeIf { it == Pair("Pong!", bob) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Terminating!")
                        node.terminateNode()
                    }
                }
            }
        }
    }

    @Test
    fun testPingPong() {
        Logger.setMinSeverity(Severity.Assert)
        executeInTestScope { node }
    }
}
