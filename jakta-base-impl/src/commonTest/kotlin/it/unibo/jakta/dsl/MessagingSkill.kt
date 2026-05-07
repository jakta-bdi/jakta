package it.unibo.jakta.dsl

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.dsl.examples.TestPingPong
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.node.Node

class BodyWithName(val name: String)
data class SimpleMessage(val payload: String, val sender: String) : AgentEvent.External.Message

interface MessagingSkill {
    fun Agent.sendMessage(payload: String, receiver: String)
}

class MessagingSkillImpl(val node: Node<BodyWithName, *>) : MessagingSkill {
    override fun Agent.sendMessage(payload: String, receiver: String) {
        node.sendEvent(
            SimpleMessage(payload, this.id.displayName),
        ) { it.name == receiver }
    }
}
