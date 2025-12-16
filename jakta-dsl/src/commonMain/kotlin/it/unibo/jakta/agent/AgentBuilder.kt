package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.environment.Skill
import it.unibo.jakta.event.Event
import it.unibo.jakta.plan.PlanLibraryBuilder
import it.unibo.jakta.plan.PlanLibraryBuilderImpl

/**
 * Builder interface for defining an agent with beliefs, goals, and plans.
 */
@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Skills: Any> {
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
    fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit)

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
    fun addBeliefPlan(plan: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, *, *, *>)

    /**
     * Adds a goal plan to the agent's plan library.
     */
    fun addGoalPlan(plan: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, *, *, *>)

    /**
     * Adds multiple belief plans to the agent's plan library.
     */
    fun withBeliefPlans(vararg plans: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, *, *, *>)

    /**
     * Adds multiple goal plans to the agent's plan library.
     */
    fun withGoalPlans(vararg plans: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, *, *, *>)

    /**
     * Defines how this agent maps [Event.External] to [Event.Internal].
     * By default, all [Event.External] are ignored.
     */
    fun eventMappingFunction(f: Event.External.() -> Event.Internal?)

    /**
     * Define the skills this agent can use in his plans.
     */
    fun skills(skills: Skills)

    /**
     * Builds and returns the agent instance.
     */
    fun build(): Agent<Belief, Goal>
}

/**
 * Implementation of the AgentBuilder interface.
 */
class AgentBuilderImpl<Belief : Any, Goal : Any, Skills: Any>(private val name: String? = null) :
    AgentBuilder<Belief, Goal, Skills> {
    private var initialBeliefs = listOf<Belief>()
    private var initialGoals = listOf<Goal>()
    private var beliefPlans = listOf<it.unibo.jakta.plan.Plan.Belief<Belief, Goal, *, *, *>>()
    private var goalPlans = listOf<it.unibo.jakta.plan.Plan.Goal<Belief, Goal, *, *, *>>()
    private var eventMappingFunction: Event.External.() -> Event.Internal? = { null }
    private lateinit var skills: Skills

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(::addBelief)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(::addGoal)
        builder.apply(block)
    }

    override fun eventMappingFunction(f: Event.External.() -> Event.Internal?) {
        this.eventMappingFunction = f
    }

    override fun skills(skills: Skills) {
        this.skills = skills
    }

    override fun hasPlans(block: PlanLibraryBuilder<Belief, Goal, Env>.() -> Unit) {
        val builder = PlanLibraryBuilderImpl(::addBeliefPlan, ::addGoalPlan)
        builder.apply(block)
    }

    override fun addBelief(belief: Belief) {
        initialBeliefs += belief
    }

    override fun addGoal(goal: Goal) {
        initialGoals += goal
    }

    override fun addBeliefPlan(plan: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, *, *, *>) {
        beliefPlans += plan
    }

    override fun addGoalPlan(plan: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, *, *, *>) {
        goalPlans += plan
    }

    override fun withBeliefPlans(vararg plans: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, *, *, *>) {
        beliefPlans += plans
    }

    override fun withGoalPlans(vararg plans: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, *, *, *>) {
        goalPlans += plans
    }




    override fun build(): Agent<Belief, Goal> = AgentImpl(
        initialBeliefs,
        initialGoals,
        beliefPlans,
        goalPlans,
        eventMappingFunction,
        skills,
        name?.let { AgentID(it) }
            ?: AgentID(),
    )
}q
