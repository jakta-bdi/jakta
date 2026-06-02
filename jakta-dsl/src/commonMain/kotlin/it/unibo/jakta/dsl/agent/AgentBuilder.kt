package it.unibo.jakta.dsl.agent

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentEvent.Internal
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.Plan

/**
 * Builder interface for defining an agent with beliefs, goals, and plans.
 */
@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Skills : Any, Body : Any> {

    /**
     * Defines how and whether a [Perception] is mapped into a [Internal] Event.
     * By default, perceptions do not generate any event.
     */
    fun handlesPerceptionEvents(handler: (Perception) -> List<Internal>?)

    /**
     * Defines how and whether a [Message] is mapped into a [Internal] Event.
     * By default, receiving messages do not generate any event.
     */
    fun handlesMessageEvents(handler: (Message) -> List<Internal>?)

    /**
     * Defines the initial beliefs of the agent using a builder block.
     */
    fun believes(block: BeliefBuilder<Belief>.() -> Unit)

    /**
     * Defines the initial goals of the agent using a builder block.
     */
    fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit)

    /**
     * Defines the plans of the agent using a plan library builder block.
     */
    fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Skills>.() -> Unit)

    /**
     * Adds a belief to the agent's initial beliefs.
     */
    fun addBelief(belief: Belief)

    /**
     * Adds a goal to the agent's initial goals.
     */
    fun addGoal(goal: Goal)

    /**
     * Adds a belief plan to the agent's plan library.
     */
    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>)

    /**
     * Adds a goal plan to the agent's plan library.
     */
    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>)

    /**
     * Adds multiple belief plans to the agent's plan library.
     */
    fun withBeliefPlans(vararg plans: Plan.Belief<Belief, Goal, Skills, *, *>)

    /**
     * Adds multiple goal plans to the agent's plan library.
     */
    fun withGoalPlans(vararg plans: Plan.Goal<Belief, Goal, Skills, *, *>)

    /**
     * Define the skills this agent can use in his plans.
     */
    fun withSkills(skillFactory: (Node<Body, Skills>) -> Skills)

    /**
     * Define how an agent can be embodied in the node.
     */
    fun embodiedAs(bodyFactory: (AgentID) -> Body)

    /**
     * Builds and returns the agent instance.
     */
    fun build(node: Node<Body, Skills>): AgentSpecification<Belief, Goal, Skills, Body>
}
