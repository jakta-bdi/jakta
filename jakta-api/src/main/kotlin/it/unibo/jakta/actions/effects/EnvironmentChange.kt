package it.unibo.jakta.actions.effects

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.messages.Message

fun interface EnvironmentChange :
    SideEffect,
    (AgentProcess) -> Unit

data class SpawnAgent<Belief : Any, Query : Any, Response>(val agent: Agent<Belief, Query, Response>) :
    EnvironmentChange {
    override fun invoke(context: AgentProcess) {
        // context.addAgent(agent)
        // TODO(Missing implementation)
    }
}

data class RemoveAgent(val agentName: String) : EnvironmentChange {
    override fun invoke(context: AgentProcess) {
//        context.removeAgent(agentName)
        // TODO(Missing implementation)
    }
}

data class SendMessage(val message: Message, val recipient: String) : EnvironmentChange {
    override fun invoke(p1: AgentProcess) {
        TODO("Not yet implemented")
    }
}

data class BroadcastMessage(val message: Message) : EnvironmentChange {
    override fun invoke(p1: AgentProcess) {
        TODO("Not yet implemented")
    }
}

data class PopMessage(val agentName: String) : EnvironmentChange {
    override fun invoke(p1: AgentProcess) {
        TODO("Not yet implemented")
    }
}

data class AddData(val key: String, val value: Any) : EnvironmentChange {
    override fun invoke(p1: AgentProcess) {
        TODO("Not yet implemented")
    }
}

data class RemoveData(val key: String) : EnvironmentChange {
    override fun invoke(p1: AgentProcess) {
        TODO("Not yet implemented")
    }
}

data class UpdateData(val newData: Map<String, Any>) : EnvironmentChange {
    override fun invoke(p1: AgentProcess) {
        TODO("Not yet implemented")
    }
}
