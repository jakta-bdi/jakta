package it.unibo.jakta.skills

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.node.BroadcastFrom
import it.unibo.jakta.node.MessageFilter
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.SendTo

/**
 * A skill that provides messaging capabilities to agents,
 * allowing them to send messages to specific receivers or broadcast messages to all other agents in the node.
 *
 * @param node The node associated with this skill.
 */
open class MessagingSkill(node: Node<Any>) : Skill<Any>(node) {

    /**
     * Sends a message with the given [payload] to the agents selected by the [filter].
     */
    fun <P : Any> Agent.send(payload: P, filter: MessageFilter<Any>) {
        node.publishEvent(Message(payload, id), filter)
    }

    /**
     * Sends a message with the given [payload] to the specified [receiver] agent.
     */
    fun <P : Any> Agent.sendTo(receiver: AgentID, payload: P) = send(payload, SendTo(receiver))

    /**
     * Broadcasts a message with the given [payload] to all other agents in the node except the sender itself.
     */
    fun <P : Any> Agent.broadcast(payload: P) = send(payload, BroadcastFrom(id))
}

/**
 * Extension function to send a message to the agents selected by the [filter] using the provided [MessagingSkill].
 */
context(skill: MessagingSkill)
fun <P : Any> Agent.send(payload: P, filter: MessageFilter<Any>) {
    with(skill) {
        send(payload, filter)
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
