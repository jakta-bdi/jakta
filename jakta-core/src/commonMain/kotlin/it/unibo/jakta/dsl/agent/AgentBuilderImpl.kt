package it.unibo.jakta.dsl.agent

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentState
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilderImpl
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.Plan
import kotlin.properties.Delegates

/**
 * Implementation of the AgentBuilder interface.
 */
class AgentBuilderImpl<Belief : Any, Goal : Any, Body : Any>(private val name: String? = null) :
    AgentBuilder<Belief, Goal, Body> {
    private var initialBeliefs = listOf<Belief>()
    private var initialGoals = listOf<Goal>()
    private var beliefPlans = listOf<Plan.Belief<Belief, Goal, *, *>>()
    private var goalPlans = listOf<Plan.Goal<Belief, Goal, *, *>>()

    private var bodyFactory: (AgentID) -> Body by Delegates.notNull()

    // By default, all messages are discarded.
    private var messageHandler: AgentState<Belief, Goal>.(Message) -> AgentUpdate<*>? = { null }

    // By default, percept do not generate events.
    private var perceptionHandler: AgentState<Belief, Goal>.(Perception) -> AgentUpdate<*>? = { null }

    override fun handlesPerceptionEvents(handler: AgentState<Belief, Goal>.(Perception) -> AgentUpdate<*>?) {
        this.perceptionHandler = handler
    }

    override fun handlesMessageEvents(handler: AgentState<Belief, Goal>.(Message) -> AgentUpdate<*>?) {
        this.messageHandler = handler
    }

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(::addBelief)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(::addGoal)
        builder.apply(block)
    }

    override fun embodiedAs(bodyFactory: (AgentID) -> Body) {
        this.bodyFactory = bodyFactory
    }

    override fun hasPlans(block: PlanLibraryBuilder<Belief, Goal>.() -> Unit) {
        val builder = PlanLibraryBuilderImpl<Belief, Goal>(::addBeliefPlan, ::addGoalPlan)
        builder.apply(block)
    }

    override fun addBelief(belief: Belief) {
        initialBeliefs += belief
    }

    override fun addGoal(goal: Goal) {
        initialGoals += goal
    }

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, *, *>) {
        beliefPlans += plan
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, *, *>) {
        goalPlans += plan
    }

    override fun withBeliefPlans(vararg plans: Plan.Belief<Belief, Goal, *, *>) {
        beliefPlans += plans
    }

    override fun withGoalPlans(vararg plans: Plan.Goal<Belief, Goal, *, *>) {
        goalPlans += plans
    }

    override fun build(node: Node<Body>): AgentSpecification<Belief, Goal, Body> =
        object : AgentSpecification<Belief, Goal, Body> {
            override val id: AgentID = this@AgentBuilderImpl.name?.let { BaseAgentID(it) } ?: BaseAgentID()
            override val body: Body = this@AgentBuilderImpl.bodyFactory(id)
            override val initialGoals: List<Goal> = this@AgentBuilderImpl.initialGoals
            override val initialState: AgentState<Belief, Goal> = BaseAgentState(
                beliefs = initialBeliefs,
                intentions = setOf(),
                beliefPlans = this@AgentBuilderImpl.beliefPlans,
                goalPlans = this@AgentBuilderImpl.goalPlans,
                perceptionHandler = perceptionHandler,
                messageHandler = messageHandler,
            )
        }
}
