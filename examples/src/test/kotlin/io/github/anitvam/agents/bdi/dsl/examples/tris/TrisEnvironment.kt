package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.environment.impl.EnvironmentImpl
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

class TrisEnvironment(
    n: Int,
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    perception: Perception = Perception.empty(),
    data: Map<String, Any> = mapOf(
        "table" to listOf(
            listOf("e", "e", "e"),
            listOf("e", "e", "e"),
            listOf("e", "e", "e"),
        ),
    ),
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data) {

    companion object {
        internal fun createGrid(n: Int) 
    }

    override fun updateData(newData: Map<String, Any>): Environment {
        return super.updateData(newData)
    }

    override fun percept(): BeliefBase {
        return super.percept()
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>
    ): Environment {
        return super.copy(agentIDs, externalActions, messageBoxes, perception, data)
    }
}

