package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.plan.Plan

/**
 * Default implementation of an [it.unibo.jakta.agent.AgentState].
 */
class BaseAgentState<Belief : Any, Goal : Any>(
    override val beliefs: Collection<Belief>,
    override val intentions: Set<Intention>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, *, *>>,
    override val perceptionHandler: AgentState<Belief, Goal>.(Perception) -> AgentUpdate<*>?,
    override val messageHandler: AgentState<Belief, Goal>.(Message<*>) -> AgentUpdate<*>?,
) : AgentState<Belief, Goal>
