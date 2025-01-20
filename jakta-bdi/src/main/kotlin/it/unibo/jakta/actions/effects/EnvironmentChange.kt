package it.unibo.jakta.actions.effects

import it.unibo.jakta.ASAgent
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.messages.Message

fun interface EnvironmentChange : ActionSideEffect, (BasicEnvironment) -> Unit

data class SpawnAgent(val agent: ASAgent) : EnvironmentChange {
    override fun invoke(context: BasicEnvironment) {
        //context.addAgent(agent)
        // TODO(Missing implementation)
    }
}

data class RemoveAgent(val agentName: String) : EnvironmentChange {
    override fun invoke(context: BasicEnvironment) {
//        context.removeAgent(agentName)
        // TODO(Missing implementation)

    }
}

data class SendMessage(
    val message: Message,
    val recipient: String,
) : EnvironmentChange {
    override fun invoke(p1: BasicEnvironment) {
        TODO("Not yet implemented")
    }
}

data class BroadcastMessage(val message: Message) : EnvironmentChange {
    override fun invoke(p1: BasicEnvironment) {
        TODO("Not yet implemented")
    }
}

data class PopMessage(val agentName: String) : EnvironmentChange {
    override fun invoke(p1: BasicEnvironment) {
        TODO("Not yet implemented")
    }
}

data class AddData(val key: String, val value: Any) : EnvironmentChange {
    override fun invoke(p1: BasicEnvironment) {
        TODO("Not yet implemented")
    }
}

data class RemoveData(val key: String) : EnvironmentChange {
    override fun invoke(p1: BasicEnvironment) {
        TODO("Not yet implemented")
    }
}

data class UpdateData(val newData: Map<String, Any>) : EnvironmentChange {
    override fun invoke(p1: BasicEnvironment) {
        TODO("Not yet implemented")
    }
}
