package it.unibo.jakta.actions.effects

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.messages.Message

fun interface EnvironmentChange<Belief : Any> : SideEffect, (AgentProcess<Belief>) -> Unit

data class SpawnAgent<Belief : Any, Query : Any, Response>(
    val agent: Agent<Belief, Query, Response>,
) : EnvironmentChange<Belief> {
    override fun invoke(context: AgentProcess<Belief>) {
        // context.addAgent(agent)
        // TODO(Missing implementation)
    }
}

data class RemoveAgent<Belief : Any>(val agentName: String) : EnvironmentChange<Belief> {
    override fun invoke(context: AgentProcess<Belief>) {
//        context.removeAgent(agentName)
        // TODO(Missing implementation)
    }
}

data class SendMessage<Belief : Any, Payload: Any>(
    val message: Message<Payload>,
    val recipient: String,
) : EnvironmentChange<Belief> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class BroadcastMessage<Belief : Any, Payload: Any>(val message: Message<Payload>) : EnvironmentChange<Belief> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class PopMessage<Belief : Any>(val agentName: String) : EnvironmentChange<Belief> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class AddData<Belief : Any>(val key: String, val value: Any) : EnvironmentChange<Belief> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class RemoveData<Belief : Any>(val key: String) : EnvironmentChange<Belief> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class UpdateData<Belief : Any>(val newData: Map<String, Any>) : EnvironmentChange<Belief> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}
