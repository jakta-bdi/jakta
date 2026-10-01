package it.unibo.jakta.situated

/**
 * A body declaring the initial state of its agent in the environment:
 * where it starts, and the initial values of its properties.
 * The implementations of [SpatialSkill] and [PropertySkill] read it when they first meet the agent,
 * then track the agent's state on their own: use the skills, not the body, for the current values.
 */
interface Situated {
    /**
     * Where the agent starts.
     */
    val initialPosition: Coordinates

    /**
     * The initial properties of the agent, see [PropertySkill].
     */
    val initialProperties: Map<String, Any?> get() = emptyMap()
}

/**
 * A [Situated] body, e.g. `embodiedAs { SituatedBody(Coordinates(0.0, 0.0)) }`.
 * It is not a data class on purpose: bodies identify agents (see `Node.getAgentIDfromBody`),
 * so two agents must not have equal bodies even when they start at the same position.
 */
class SituatedBody(
    override val initialPosition: Coordinates,
    override val initialProperties: Map<String, Any?> = emptyMap(),
) : Situated
