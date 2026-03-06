package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.agent.basImpl.BaseAgentID
import it.unibo.jakta.agent.basImpl.BaseAgentState
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentEvent.Internal
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanLibraryBuilder
import it.unibo.jakta.plan.PlanLibraryBuilderImpl
import kotlin.properties.Delegates

/**
 * Builder interface for defining an agent with beliefs, goals, and plans.
 */
@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Skills : Any, Body : Any> {

    /**
     * Defines the body of the agent, i.e. the information visible to other agents and that can modified by their skill.
     */
    var body: Body

    /**
     * Defines how and whether a [Perception] is mapped into a [Internal] Event.
     * By default, perceptions do not generate any event.
     */
    var perceptionHandler: (Perception) -> Internal?

    /**
     * Defines how and whether a [Message] is mapped into a [Internal] Event.
     * By default, receiving messages do not generate any event.
     */
    var messageHandler: (Message) -> Internal?

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
     * Defines how this agent maps [AgentEvent.External] to [AgentEvent.Internal].
     * By default, all [AgentEvent.External] are ignored.
     */
    fun eventMappingFunction(f: AgentEvent.External.() -> AgentEvent.Internal?)

    /**
     * Define the skills this agent can use in his plans.
     */
    fun withSkills(skillFactory: () -> Skills)

    /**
     * Builds and returns the agent instance.
     */
    fun build(): AgentSpecification<Belief, Goal, Skills, Body>
}

/**
 * Implementation of the AgentBuilder interface.
 */
class AgentBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Body : Any>(private val name: String? = null) :
    AgentBuilder<Belief, Goal, Skills, Body> {
    private var initialBeliefs = listOf<Belief>()
    private var initialGoals = listOf<Goal>()
    private var beliefPlans = listOf<Plan.Belief<Belief, Goal, Skills, *, *>>()
    private var goalPlans = listOf<Plan.Goal<Belief, Goal, Skills, *, *>>()
    private var eventMappingFunction: AgentEvent.External.() -> AgentEvent.Internal? = { null }
    private lateinit var skillsFactory: () -> Skills // TODO improve

    override var body: Body by Delegates.notNull()
    override var messageHandler: (Message) -> Internal? = { null } // By default, all messages are discarded.
    override var perceptionHandler: (Perception) -> Internal? = { null } // By default, percept do not generate events.

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(::addBelief)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(::addGoal)
        builder.apply(block)
    }

    override fun eventMappingFunction(f: AgentEvent.External.() -> AgentEvent.Internal?) {
        this.eventMappingFunction = f
    }

    override fun withSkills(skillFactory: () -> Skills) {
        this.skillsFactory = skillFactory
    }

    override fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Skills>.() -> Unit) {
        val builder = PlanLibraryBuilderImpl<Belief, Goal, Skills>(::addBeliefPlan, ::addGoalPlan)
        builder.apply(block)
    }

    override fun addBelief(belief: Belief) {
        initialBeliefs += belief
    }

    override fun addGoal(goal: Goal) {
        initialGoals += goal
    }

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>) {
        beliefPlans += plan
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>) {
        goalPlans += plan
    }

    override fun withBeliefPlans(vararg plans: Plan.Belief<Belief, Goal, Skills, *, *>) {
        beliefPlans += plans
    }

    override fun withGoalPlans(vararg plans: Plan.Goal<Belief, Goal, Skills, *, *>) {
        goalPlans += plans
    }

    override fun build(): AgentSpecification<Belief, Goal, Skills, Body> =
        object : AgentSpecification<Belief, Goal, Skills, Body> {
            override val body: Body = this@AgentBuilderImpl.body
            override val initialState: AgentState<Belief, Goal, Skills> = BaseAgentState(
                beliefs = initialBeliefs,
                intentions = setOf(),
                beliefPlans = this@AgentBuilderImpl.beliefPlans,
                goalPlans = this@AgentBuilderImpl.goalPlans,
                perceptionHandler = perceptionHandler,
                messageHandler = messageHandler,
                skills = skillsFactory(),
            )
            override val initialGoals: List<Goal> = this@AgentBuilderImpl.initialGoals
            override val id: AgentID = this@AgentBuilderImpl.name?.let { BaseAgentID(it) } ?: BaseAgentID()
        }
}
