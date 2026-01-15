package it.unibo.jakta.agent.basImpl

import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.event.Event.External.Message
import it.unibo.jakta.event.Event.External.Perception
import it.unibo.jakta.event.Event.Internal
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.plan.Plan

/**
 * Default implementation of an [AgentState].
 */
internal class BaseAgentState<Belief: Any, Goal: Any, Skills: Any>(
    override val beliefs: Collection<Belief>,
    override val intentions: Set<Intention>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>,
    override val perceptionHandler: (Perception) -> Internal?,
    override val messageHandler: (Message) -> Internal?,
    override val skills: Skills,
): AgentState<Belief, Goal, Skills>

