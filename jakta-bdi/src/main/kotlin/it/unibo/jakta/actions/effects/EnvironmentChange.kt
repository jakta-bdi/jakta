package it.unibo.jakta.actions.effects

import it.unibo.jakta.ASAgent
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.messages.Message

// Potrebbero diventare dipendenti dalle capabilities ?

sealed interface EnvironmentChange : ActionResult<Environment>

data class SpawnAgent(val agent: ASAgent) : EnvironmentChange {
     override fun Environment.apply(controller: Activity.Controller?) {
        addAgent(agent)
    }
}

data class RemoveAgent(val agentName: String) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        removeAgent(agentName)
    }
}

data class SendMessage(
    val message: Message,
    val recipient: String,
) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        TODO("Missing Implementation")
    }
}

data class BroadcastMessage(val message: Message) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        TODO("Not yet implemented")
    }
}

data class PopMessage(val agentName: String) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        TODO("Not yet implemented")
    }
}

data class AddData(val key: String, val value: Any) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        TODO("Not yet implemented")
    }
}

data class RemoveData(val key: String) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        TODO("Not yet implemented")
    }
}

data class UpdateData(val newData: Map<String, Any>) : EnvironmentChange {
    override fun Environment.apply(controller: Activity.Controller?) {
        TODO("Not yet implemented")
    }
}
