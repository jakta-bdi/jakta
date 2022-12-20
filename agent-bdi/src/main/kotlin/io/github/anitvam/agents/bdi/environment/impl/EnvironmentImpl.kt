package io.github.anitvam.agents.bdi.environment.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

internal data class EnvironmentImpl(
    override val agents: Set<Agent>,
    override val externalActions: Map<String, ExternalAction>,
    override val messageBoxes: Map<String, MessageQueue>,
    private val perceptInvocation: () -> Perception,
) : Environment {

    private val lifecycles: List<AgentLifecycle> = agents.map { AgentLifecycle.of(it) }

    override fun percept(): Perception = perceptInvocation()

    override fun runAgents() {
        lifecycles.forEach { it.reason(this) }
        // Manage effects on environments as SideEffects
    }
}
