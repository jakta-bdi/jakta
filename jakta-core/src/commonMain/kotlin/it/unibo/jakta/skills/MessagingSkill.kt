package it.unibo.jakta.skills

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.node.Node

/**
 * A skill that provides messaging capabilities to agents,
 * allowing them to send messages to specific receivers or broadcast messages to all other agents in the node.
 *
 * @param node The node associated with this skill.
 */
open class MessagingSkill(node: Node<Any>) : Skill<Any>(node) {

    /**
     * Sends a message with the given [payload] to the specified [receiver] agent.
     */
    fun <P : Any> Agent.sendTo(receiver: AgentID, payload: P) {
        node.sendEvent(Message(payload, id)) { body ->
            node.getAgentIDfromBody(body) == receiver
        }
    }

    /**
     * Broadcasts a message with the given [payload] to all other agents in the node except the sender itself.
     */
    fun <P : Any> Agent.broadcast(payload: P) {
        node.sendEvent(Message(payload, id)) { body ->
            node.getAgentIDfromBody(body) != id
        }
    }
}

/**
 * Extension function to send a message to a specific receiver using the provided [MessagingSkill].
 * This allows for a more concise syntax when sending messages.
 */
context(skill: MessagingSkill)
fun <P : Any> Agent.sendTo(receiver: AgentID, payload: P) {
    with(skill) {
        sendTo(receiver, payload)
    }
}

/**
 * Extension function to broadcast a message to all other agents using the provided [MessagingSkill].
 * This allows for a more concise syntax when broadcasting messages.
 */
context(skill: MessagingSkill)
fun <P : Any> Agent.broadcast(payload: P) {
    with(skill) {
        broadcast(payload)
    }
}
