@file:JvmName("TwoAgentsSameNode")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.node.Node

class BodyWithName(val name: String)
data class SimpleMessage(val payload: String, val sender: String) : AgentEvent.External.Message
fun String.ifGoalMatch(goal: String): Unit? = if (this == goal) Unit else null

class MessagingSkill(val node: Node<BodyWithName, *>) {
    fun Agent.sendMessage(payload: String, receiver: String) {
        node.sendEvent(
            SimpleMessage(payload, this.id.displayName),
        ) { it.name == receiver }
    }
}

private fun <Belief : Any, Goal : Any> LocalNodeBuilder<Belief, Goal, MessagingSkill, BodyWithName>.messageEnabledAgent(
    name: String,
    block: AgentBuilder<Belief, Goal, MessagingSkill, BodyWithName>.() -> Unit,
) {
    agent(name) {
        embodiedAs { BodyWithName(name) }
        withSkills { MessagingSkill(it) }
        handlesMessageEvents { BeliefAddEvent(it) }
        block()
    }
}

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(LocalNodeBuilder()) {
    node {
        messageEnabledAgent("Bob") {
            // withBeliefsType<SimpleMessage>() //TODO(Does this work instead of specifying the believes {}
            believes {
                +SimpleMessage("pippo", "pluto") // TODO(It cannot infer Belief type from plans directly)
            }

            hasPlans {
                adding.belief {
                    this.takeIf { it == SimpleMessage("Message", "Alice") }
                } triggers {
                    agent.print("Message received, replying...")
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
                    agent.print("Termination of Ping Pong!")
                }
            }
        }
    }
}
