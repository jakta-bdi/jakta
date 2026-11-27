package it.unibo.jakta.artifact

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.plan.PlanScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The skill to use artifacts in plans: create them, look them up and focus on them, wherever they are hosted.
 * The operations are invoked on the artifacts, e.g. `counter.inc()`,
 * and the agents focusing on an artifact perceive its observable properties and signals as [ArtifactEvent]s.
 *
 * Install it around the agents of an [ArtifactNode], e.g. `context(ArtifactSkill(node)) { agent { ... } }`.
 */
class ArtifactSkill(private val node: ArtifactNode<*>) {

    /**
     * Hosts the [artifact] on the node of the agent, i.e. CArtAgO's `makeArtifact`.
     */
    fun <A : Artifact> make(artifact: A): A = node.makeArtifact(artifact)

    /**
     * Finds the artifact with the given [name], hosted by this node or by another one within the [timeout].
     * If it is hosted by another node, the [factory] creates its local reference, e.g. `::Counter`.
     */
    suspend fun <A : Artifact> lookup(name: String, factory: (String) -> A, timeout: Duration = 10.seconds): A {
        @Suppress("UNCHECKED_CAST")
        return node.lookup(name, factory, timeout) as A
    }

    /**
     * Starts perceiving the observable properties and the signals of the [artifact] as [ArtifactEvent]s.
     * The current values of the observable properties are perceived before this function returns.
     */
    suspend fun Agent.focus(artifact: Artifact) = node.focus(artifact, id)

    /**
     * Stops perceiving the [artifact]. Its observable properties are perceived as [ArtifactEvent.PropertyRemoved].
     */
    suspend fun Agent.stopFocus(artifact: Artifact) = node.stopFocus(artifact, id)

    /**
     * Suspends the intention until the [artifact], which the agent focuses on, emits the [signal].
     */
    suspend fun MutableAgentState<*, *>.awaitSignal(artifact: Artifact, signal: String): ArtifactEvent.Signal =
        checkNotNull(
            wait({ event ->
                (event as? ArtifactEvent.Signal)?.takeIf { it.artifact == artifact.name && it.signal == signal }
            }),
        )
}

/**
 * The [ArtifactSkill] in plans, e.g. `artifacts.lookup("counter", ::Counter)`.
 */
context(skill: ArtifactSkill)
val PlanScope<*, *, *>.artifacts: ArtifactSkill
    get() = skill

/**
 * Starts perceiving the [artifact], see [ArtifactSkill.focus].
 */
context(skill: ArtifactSkill)
suspend fun Agent.focus(artifact: Artifact) = with(skill) { focus(artifact) }

/**
 * Stops perceiving the [artifact], see [ArtifactSkill.stopFocus].
 */
context(skill: ArtifactSkill)
suspend fun Agent.stopFocus(artifact: Artifact) = with(skill) { stopFocus(artifact) }

/**
 * Waits for a [signal] of the [artifact], see [ArtifactSkill.awaitSignal].
 */
context(skill: ArtifactSkill)
suspend fun MutableAgentState<*, *>.awaitSignal(artifact: Artifact, signal: String) =
    with(skill) { awaitSignal(artifact, signal) }
