package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL

/**
 * Builder interface for defining triggers based on belief and goal events.
 */
@JaktaDSL
sealed interface TriggerBuilder<Belief : Any, Goal : Any, Skills : Any> {

    /**
     * Builder interface for defining triggers based on belief additions and goal additions.
     */
    sealed interface Addition<Belief : Any, Goal : Any, Skills : Any> : TriggerBuilder<Belief, Goal, Skills> {

        /**
         * Given a @param[beliefQuery] as a function that matches a belief
         * and extracts a context from it if the belief matches.
         * @return a plan builder for belief addition triggers.
         */
        fun <Context : Any> belief(
            beliefQuery: Belief.() -> Context?,
        ): PlanBuilder.Addition.Belief<Belief, Goal, Skills, Context>

        /**
         * Given a @param[goalQuery] as a function that matches a goal
         * and extracts a context from it if the goal matches.
         * @return a plan builder for goal addition triggers.
         */
        fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Addition.Goal<Belief, Goal, Skills, Context>
    }

    /**
     * Builder interface for defining triggers based on belief removals and goal removals.
     */
    sealed interface Removal<Belief : Any, Goal : Any, Skills : Any> : TriggerBuilder<Belief, Goal, Skills> {

        /**
         * Given a @param[beliefQuery] as a function that matches a belief
         * and extracts a context from it if the belief matches.
         * @return a plan builder for belief removal triggers.
         */
        fun <Context : Any> belief(
            beliefQuery: Belief.() -> Context?,
        ): PlanBuilder.Removal.Belief<Belief, Goal, Skills, Context>

        /**
         * Given a @param[goalQuery] as a function that matches a goal
         * and extracts a context from it if the goal matches.
         * @return a plan builder for goal removal triggers.
         */
        fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Removal.Goal<Belief, Goal, Skills, Context>
    }

    /**
     * Builder interface for defining triggers based on goal failure interceptions.
     */
    sealed interface FailureInterception<Belief : Any, Goal : Any, Skills : Any> :
        TriggerBuilder<Belief, Goal, Skills> {

        /**
         * Given a @param[goalQuery] as a function that matches a goal
         * and extracts a context from it if the goal matches.
         * @return a plan builder for goal failure interception triggers.
         */
        fun <Context : Any> goal(
            goalQuery: Goal.() -> Context?,
        ): PlanBuilder.FailureInterception.Goal<Belief, Goal, Skills, Context>
    }
}

/**
 * Implementation of the TriggerBuilder for belief additions and goal additions.
 */
class TriggerAdditionImpl<Belief : Any, Goal : Any, Skills : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Skills, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,

) : TriggerBuilder.Addition<Belief, Goal, Skills> {

    override fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Addition.Belief<Belief, Goal, Skills, Context> =
        BeliefAdditionPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(
        goalQuery: Goal.() -> Context?,
    ): PlanBuilder.Addition.Goal<Belief, Goal, Skills, Context> = GoalAdditionPlanBuilderImpl(addGoalPlan, goalQuery)
}

/**
 * Implementation of the TriggerBuilder for belief removals and goal removals.
 */
class TriggerRemovalImpl<Belief : Any, Goal : Any, Skills : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Skills, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,
) : TriggerBuilder.Removal<Belief, Goal, Skills> {

    override fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Removal.Belief<Belief, Goal, Skills, Context> = BeliefRemovalPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(
        goalQuery: Goal.() -> Context?,
    ): PlanBuilder.Removal.Goal<Belief, Goal, Skills, Context> = GoalRemovalPlanBuilderImpl(addGoalPlan, goalQuery)
}

/**
 * Implementation of the TriggerBuilder for goal failure interceptions.
 */
class TriggerFailureInterceptionImpl<Belief : Any, Goal : Any, Skills : Any>(
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,
) : TriggerBuilder.FailureInterception<Belief, Goal, Skills> {

    override fun <Context : Any> goal(
        goalQuery: Goal.() -> Context?,
    ): PlanBuilder.FailureInterception.Goal<Belief, Goal, Skills, Context> =
        GoalFailurePlanBuilderImpl(addGoalPlan, goalQuery)
}
