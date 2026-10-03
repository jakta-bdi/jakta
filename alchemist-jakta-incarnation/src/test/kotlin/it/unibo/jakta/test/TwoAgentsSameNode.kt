@file:JvmName("TwoAgentsSameNode")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.alchemistNode
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.JaktaForAlchemistNode
import it.unibo.jakta.skills.InMemoryDirectory
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.lookup
import it.unibo.jakta.skills.sendTo
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

fun Pair<String, AgentID>.isFrom(text: String, name: String): Boolean = first == text && second.name == name

fun <Goal : Any> BaseNodeBuilder<Any, JaktaForAlchemistNode<Any>>.messageEnabledAgent(
    name: String,
    block: AgentBuilder<Pair<String, AgentID>, Goal, Any>.() -> Unit,
) {
    agent(name) {
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

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(NodeBuilders.alchemistNode()) {
    node {
        context(MessagingSkill(node), InMemoryDirectory().skillFor(node)) {
            messageEnabledAgent("Bob") {
                hasPlanLibrary {
                    adding.belief {
                        this.takeIf { it.isFrom("Ping!", "Alice") }
                    } triggers {
                        val (message, sender) = context
                        agent.print("Received: \"$message\" from $sender")
                        agent.print("Sending pong to Alice")
                        agent.sendTo(sender, "Pong!")
                    }
                }
            }
            messageEnabledAgent("Alice") {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlanLibrary {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("Hello World!")
                        agent.print("Time: ${alchemistEnvironment.simulation.time}")
                        delay(5000.milliseconds)
                        agent.print("Sending ping to Bob")
                        agent.sendTo(agent.lookup("Bob").single(), "Ping!")
                        agent.print("Time after delay of 5000: ${alchemistEnvironment.simulation.time}")
                    }
                    adding.belief {
                        this.takeIf { it.isFrom("Pong!", "Bob") }
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
