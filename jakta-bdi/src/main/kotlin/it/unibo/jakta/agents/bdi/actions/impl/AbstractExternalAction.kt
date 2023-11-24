package it.unibo.jakta.agents.bdi.actions.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.ExternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.AddData
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.RemoveAgent
import it.unibo.jakta.agents.bdi.actions.effects.RemoveData
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.bdi.actions.effects.SpawnAgent
import it.unibo.jakta.agents.bdi.actions.effects.UpdateData
import it.unibo.jakta.agents.bdi.messages.Message
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
