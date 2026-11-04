@file:JvmName("PingPong")

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.alchemistNode
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private data class ToldBelief(val text: String, val sender: AgentID)

private val alice = BaseAgentID("Alice")
private val bob = BaseAgentID("Bob")

private fun AgentBuilder<ToldBelief, String, Any>.receivesTextMessages() {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        when (val payload = message.payload) {
            is String -> AgentUpdate.Belief(setOf(ToldBelief(payload, message.sender)), emptySet())
            else -> null
        }
    }
}

/**
 * Hits the ball back: when told [ball], waits a second of simulated time and sends [back] to the sender.
 */
context(_: MessagingSkill, runtime: JaktaForAlchemistRuntime<*>)
private fun AgentBuilder<ToldBelief, String, Any>.returns(ball: String, back: String) = hasPlanLibrary {
    adding.belief {
        takeIf { it.text == ball }
    } triggers {
        agent.forget(context)
        val time = runtime.alchemistEnvironment.simulation.time
        agent.print("Got \"$ball\" from ${context.sender.displayName} at time $time")
        delay(1.seconds)
        agent.sendTo(context.sender, back)
    }
}

/**
 * The device of Bob, who answers each ping with a pong.
 */
fun <P : Position<P>> JaktaForAlchemistRuntime<P>.bobDevice() = device(NodeBuilders.alchemistNode()) {
    node {
        context(MessagingSkill(node), this@bobDevice) {
            agent(bob) {
                receivesTextMessages()
                returns("Ping!", "Pong!")
            }
        }
    }
}

/**
 * The device of Alice, who serves the first ping, and answers each pong with a ping.
 */
fun <P : Position<P>> JaktaForAlchemistRuntime<P>.aliceDevice() = device(NodeBuilders.alchemistNode()) {
    node {
        context(MessagingSkill(node), this@aliceDevice) {
            agent(alice) {
                receivesTextMessages()
                hasInitialGoals {
                    !"serve"
                }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "serve" }
                    } triggers {
                        agent.sendTo(bob, "Ping!")
                    }
                }
                returns("Pong!", "Ping!")
            }
        }
    }
}
