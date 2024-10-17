package it.unibo.jakta.actions.impl

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.ExternalAction
import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.ExternalResponse
import it.unibo.jakta.actions.effects.AddData
import it.unibo.jakta.actions.effects.BroadcastMessage
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.effects.RemoveAgent
import it.unibo.jakta.actions.effects.RemoveData
import it.unibo.jakta.actions.effects.SendMessage
import it.unibo.jakta.actions.effects.SpawnAgent
import it.unibo.jakta.actions.effects.UpdateData
import it.unibo.jakta.messages.Message
import it.unibo.tuprolog.solve.Signature

abstract class AbstractExternalAction(override val signature: Signature) : ExternalAction,
    AbstractAction<EnvironmentChange, ExternalResponse, ExternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    override fun addAgent(agent: Agent) {
        effects.add(SpawnAgent(agent))
    }

    override fun removeAgent(agentName: String) {
        effects.add(RemoveAgent(agentName))
    }

    override fun sendMessage(agentName: String, message: Message) {
        effects.add(SendMessage(message, agentName))
    }

    override fun broadcastMessage(message: Message) {
        effects.add(BroadcastMessage(message))
    }

    override fun addData(key: String, value: Any) {
        effects.add(AddData(key, value))
    }

    override fun removeData(key: String) {
        effects.add(RemoveData(key))
    }

    override fun updateData(newData: Map<String, Any>) {
        effects.add(UpdateData(newData))
    }

    override fun toString(): String = "ExternalAction(${signature.name}, ${signature.arity})"
}
