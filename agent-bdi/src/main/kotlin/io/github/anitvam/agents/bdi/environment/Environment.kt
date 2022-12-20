package io.github.anitvam.agents.bdi.environment

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.environment.impl.EnvironmentImpl
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

interface Environment {
    val agents: Set<Agent>

    val messageBoxes: Map<String, MessageQueue>

    val externalActions: Map<String, ExternalAction>

    fun percept(): Perception

    fun runAgents()

    fun copy(
        agents: Set<Agent> = this.agents,
        externalActions: Map<String, ExternalAction> = this.externalActions,
        messageBoxes: Map<String, MessageQueue> = this.messageBoxes,
    ) = of(agents, externalActions, messageBoxes) { this.percept() }

    companion object {
        fun of(
            agents: Set<Agent>,
            externalActions: Map<String, ExternalAction> = emptyMap(),
            messageBoxes: Map<String, MessageQueue> = emptyMap(),
            perceptInvocation: () -> Perception = { Perception.empty() },
        ): Environment = EnvironmentImpl(agents, externalActions, messageBoxes, perceptInvocation)
    }
}
