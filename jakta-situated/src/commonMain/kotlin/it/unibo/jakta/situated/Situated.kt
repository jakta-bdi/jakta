package it.unibo.jakta.situated

/**
 * A body situating its agent in the environment: where it is, and the initial values of its properties.
 * The implementations of [SpatialSkill] keep [position] up to date: it is where the agent currently is.
 * Properties are tracked by the implementations of [PropertySkill]: use the skill for their current values.
 */
interface Situated {
    /**
     * Where the agent is, initially where it starts.
     * It is updated by the [SpatialSkill] of the agent: move the agent with the skill, not by setting it.
     */
    var position: Coordinates

    /**
     * The initial properties of the agent, see [PropertySkill].
     */
    val initialProperties: Map<String, Any?> get() = emptyMap()
}

/**
 * A [Situated] body, e.g. `embodiedAs { SituatedBody(Coordinates(0.0, 0.0)) }`.
 * It is not a data class on purpose: bodies identify agents (see `Node.getAgentIDfromBody`),
 * so two agents must not have equal bodies even when they are at the same position.
 */
class SituatedBody(
    override var position: Coordinates,
    override val initialProperties: Map<String, Any?> = emptyMap(),
) : Situated
