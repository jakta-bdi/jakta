@file:JvmName("TwoAgentsSameNode")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

private fun <Goal : Any> BaseNodeBuilder<Any, ExecutableNode<Any>>.messageEnabledAgent(
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

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(NodeBuilders.baseNode()) {
    node {
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
                        agent.print("Hello World!")
                        agent.print("Time: ${alchemistEnvironment.simulation.time}")
                        delay(5000.milliseconds)
                        agent.print("Sending ping to Bob")
                        agent.sendTo(bob, "Ping!")
                        agent.print("Time after delay of 5000: ${alchemistEnvironment.simulation.time}")
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
}
