package it.unibo.jakta.agent

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.plan.PlanLibraryBuilder
import it.unibo.jakta.plan.PlanLibraryBuilderImpl

/**
 * Builder interface for defining an agent with beliefs, goals, and plans.
 */
@JaktaDSL
interface AgentBuilder<Belief : Any, Goal : Any, Env : Environment> {
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
    fun addBeliefPlan(plan: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, Env, *, *>)

    /**
     * Adds a goal plan to the agent's plan library.
     */
    fun addGoalPlan(plan: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, Env, *, *>)

    /**
     * Adds multiple belief plans to the agent's plan library.
     */
    fun withBeliefPlans(vararg plans: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, Env, *, *>)

    /**
     * Adds multiple goal plans to the agent's plan library.
     */
    fun withGoalPlans(vararg plans: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, Env, *, *>)

    /**
     * Builds and returns the agent instance.
     */
    fun build(): Agent<Belief, Goal, Env>
}

/**
 * Implementation of the AgentBuilder interface.
 */
class AgentBuilderImpl<Belief : Any, Goal : Any, Env : Environment>(private val name: String? = null) :
    AgentBuilder<Belief, Goal, Env> {
    private var initialBeliefs = listOf<Belief>()
    private var initialGoals = listOf<Goal>()
    private var beliefPlans = listOf<it.unibo.jakta.plan.Plan.Belief<Belief, Goal, Env, *, *>>()
    private var goalPlans = listOf<it.unibo.jakta.plan.Plan.Goal<Belief, Goal, Env, *, *>>()

    override fun believes(block: BeliefBuilder<Belief>.() -> Unit) {
        val builder = BeliefBuilderImpl(::addBelief)
        builder.apply(block)
    }

    override fun hasInitialGoals(block: GoalBuilder<Goal>.() -> Unit) {
        val builder = GoalBuilderImpl(::addGoal)
        builder.apply(block)
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

    override fun addBeliefPlan(plan: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, Env, *, *>) {
        beliefPlans += plan
    }

    override fun addGoalPlan(plan: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, Env, *, *>) {
        goalPlans += plan
    }

    override fun withBeliefPlans(vararg plans: it.unibo.jakta.plan.Plan.Belief<Belief, Goal, Env, *, *>) {
        beliefPlans += plans
    }

    override fun withGoalPlans(vararg plans: it.unibo.jakta.plan.Plan.Goal<Belief, Goal, Env, *, *>) {
        goalPlans += plans
    }

    override fun build(): Agent<Belief, Goal, Env> = AgentImpl(
        initialBeliefs,
        initialGoals,
        beliefPlans,
        goalPlans,
        name?.let { AgentID(it) }
            ?: AgentID(),
    )
}
