package it.unibo.jakta.actions.effects

import it.unibo.jakta.ASAgent
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.messages.Message

// Potrebbero diventare dipendenti dalle capabilities ?

fun interface EnvironmentChange : ActionSideEffect, (Environment) -> Unit

data class SpawnAgent(val agent: ASAgent) : EnvironmentChange {
    override fun invoke(context: Environment) {
        context.addAgent(agent)
    }
}

data class RemoveAgent(val agentName: String) : EnvironmentChange {
    override fun invoke(context: Environment) {
        context.removeAgent(agentName)
    }
}

data class SendMessage(
    val message: Message,
    val recipient: String,
) : EnvironmentChange {
    override fun invoke(p1: Environment) {
        TODO("Not yet implemented")
    }
}

data class BroadcastMessage(val message: Message) : EnvironmentChange {
    override fun invoke(p1: Environment) {
        TODO("Not yet implemented")
    }
}

data class PopMessage(val agentName: String) : EnvironmentChange {
    override fun invoke(p1: Environment) {
        TODO("Not yet implemented")
    }
}

data class AddData(val key: String, val value: Any) : EnvironmentChange {
    override fun invoke(p1: Environment) {
        TODO("Not yet implemented")
    }
}

data class RemoveData(val key: String) : EnvironmentChange {
    override fun invoke(p1: Environment) {
        TODO("Not yet implemented")
    }
}

data class UpdateData(val newData: Map<String, Any>) : EnvironmentChange {
    override fun invoke(p1: Environment) {
        TODO("Not yet implemented")
    }
}
