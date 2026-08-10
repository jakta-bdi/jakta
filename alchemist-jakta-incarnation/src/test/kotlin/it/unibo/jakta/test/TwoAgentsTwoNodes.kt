@file:JvmName("TwoAgentsTwoNodes")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.alchemistNode
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo

val bob = BaseAgentID("Bob")
val alice = BaseAgentID("Alice")

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypointNodeOne() = device(
    NodeBuilders.alchemistNode(),
) {
    node {
        context(MessagingSkill(node)) {
            messageEnabledAgent(bob) {
                hasPlanLibrary {
                    adding.belief {
                        this.takeIf { it == Pair("Ping!", alice) }
                    } triggers {
                        val (message, sender) = context
                        agent.print("I'm Bob from ${node.id}. I Received: \"$message\" from $sender")
                        agent.print("Sending pong back to Alice...")
                        agent.sendTo(sender, "Pong!")
                    }
                }
            }
        }
    }
}

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypointNodeTwo() = device(
    NodeBuilders.alchemistNode(),
) {
    node {
        context(MessagingSkill(node)) {
            messageEnabledAgent(alice) {
                hasInitialGoals {
                    !"sendMessage"
                }
                hasPlanLibrary {
                    adding.goal {
                        ifGoalMatch("sendMessage")
                    } triggers {
                        agent.print("I'm Alice from node ${node.id}. Sending ping to Bob...")
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
}
