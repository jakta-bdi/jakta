package it.unibo.jakta.actions.effects

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.messages.Message

fun interface EnvironmentChange<Belief : Any, Query : Any, Response> : SideEffect<Belief, Query, Response>, (AgentProcess<Belief>) -> Unit

data class SpawnAgent<Belief : Any, Query : Any, Response>(
    val agent: Agent<Belief, Query, Response>,
) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(context: AgentProcess<Belief>) {
        // context.addAgent(agent)
        // TODO(Missing implementation)
    }
}

data class RemoveAgent<Belief : Any, Query : Any, Response>(val agentName: String) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(context: AgentProcess<Belief>) {
//        context.removeAgent(agentName)
        // TODO(Missing implementation)
    }
}

data class SendMessage<Belief : Any, Query : Any, Response, Payload: Any>(
    val message: Message<Payload>,
    val recipient: String,
) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class BroadcastMessage<Belief: Any, Query : Any, Response, Payload: Any>(val message: Message<Payload>) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class PopMessage<Belief : Any, Query : Any, Response>(val agentName: String) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class AddData<Belief : Any, Query : Any, Response>(val key: String, val value: Any) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class RemoveData<Belief : Any, Query : Any, Response>(val key: String) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}

data class UpdateData<Belief : Any, Query : Any, Response>(val newData: Map<String, Any>) : EnvironmentChange<Belief, Query, Response> {
    override fun invoke(p1: AgentProcess<Belief>) {
        TODO("Not yet implemented")
    }
}
