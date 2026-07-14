package it.unibo.jakta.event

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeID

/**
 * Base implementation of [SystemEvent.AgentAddition].
 */
data class AgentAdditionEvent<Belief : Any, Goal : Any>(
    override val executableAgent: ExecutableAgent<Belief, Goal>,
    override val nodeID: NodeID,
) : SystemEvent.AgentAddition<Belief, Goal>

/**
 * Base implementation of [SystemEvent.AgentRemoval].
 */
data class AgentRemovalEvent(override val id: AgentID) : SystemEvent.AgentRemoval

/**
 * Base implementation of [SystemEvent.ShutDownNode].
 */
data class ShutDownNodeEvent(override val nodeID: NodeID) : SystemEvent.ShutDownNode

/**
 * Base implementation of [SystemEvent.AgentMessage].
 */
data class AgentMessageEvent<P : Any, Body : Any>(
    override val message: AgentEvent.External.Message<P>,
    override val filterFunction: Node<Body>.(Body) -> Boolean,
) : SystemEvent.AgentMessage<P, Body>
