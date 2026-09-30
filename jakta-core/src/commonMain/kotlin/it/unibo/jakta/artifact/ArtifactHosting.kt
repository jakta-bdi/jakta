package it.unibo.jakta.artifact

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node

/**
 * Hosts the [artifact] on this node: its operations will be executed here.
 * @return the [artifact], to use it in the agents' plans.
 */
fun <A : Artifact> Node<Any>.makeArtifact(artifact: A): A = artifact.also { addAgent(host(it, mirror = false)) }

/**
 * Makes the [artifact], hosted by another node with [makeArtifact], usable by the agents of this node.
 * Operations are forwarded to the host node, and the observable properties and signals of the artifact
 * are delivered as perceptions to the agents of this node focusing on it.
 * @param artifact a new instance of the artifact class, with the same name of the hosted one.
 * @return the [artifact], to use it in the agents' plans.
 */
fun <A : Artifact> Node<Any>.mirrorArtifact(artifact: A): A = artifact.also { addAgent(host(it, mirror = true)) }

/**
 * Starts perceiving the observable properties and the signals of the [artifact] as [ArtifactEvent]s.
 * The current values of the observable properties are perceived right away.
 */
suspend fun Agent.focus(artifact: Artifact) = artifact.focus(id)

/**
 * Stops perceiving the [artifact]. The beliefs already derived from its perceptions are kept.
 */
suspend fun Agent.stopFocus(artifact: Artifact) = artifact.stopFocus(id)

// The artifact runs on a node as an agent whose goals are the tasks to execute:
// the agent's reasoning cycle serializes them, and the node runner executes it as any other agent.
private fun host(artifact: Artifact, mirror: Boolean) = agent<Unit, ArtifactTask, Any>(
    if (mirror) BaseAgentID("${artifact.name}@mirror") else Artifact.homeId(artifact.name),
) {
    embodiedAs { artifact }
    addGoal(ArtifactTask { artifact.attach(Artifact.Runtime(node, it, mirror)) })
    handlesMessageEvents { message -> artifact.taskFor(message)?.let { AgentUpdate.Goal(setOf(it)) } }
    hasPlanLibrary {
        adding.goal { this } triggers { context.block(agent) }
    }
}
