package it.unibo.jakta.dsl.plan

import it.unibo.jakta.dsl.JaktaDSL

/**
 * Builder interface for defining triggers based on belief and goal events.
 */
@JaktaDSL
interface TriggerBuilder<Belief : Any, Goal : Any> {

    /**
     * Builder interface for defining triggers based on belief additions and goal additions.
     */
    interface Addition<Belief : Any, Goal : Any> : TriggerBuilder<Belief, Goal> {

        /**
         * Given a @param[beliefQuery] as a function that matches a belief
         * and extracts a context from it if the belief matches.
         * @return a plan builder for belief addition triggers.
         */
        fun <Context : Any> belief(
            beliefQuery: Belief.() -> Context?,
        ): PlanBuilder.Addition.Belief<Belief, Goal, Context>

        /**
         * Given a @param[goalQuery] as a function that matches a goal
         * and extracts a context from it if the goal matches.
         * @return a plan builder for goal addition triggers.
         */
        fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Addition.Goal<Belief, Goal, Context>
    }

    /**
     * Builder interface for defining triggers based on belief removals and goal removals.
     */
    interface Removal<Belief : Any, Goal : Any> : TriggerBuilder<Belief, Goal> {

        /**
         * Given a @param[beliefQuery] as a function that matches a belief
         * and extracts a context from it if the belief matches.
         * @return a plan builder for belief removal triggers.
         */
        fun <Context : Any> belief(
            beliefQuery: Belief.() -> Context?,
        ): PlanBuilder.Removal.Belief<Belief, Goal, Context>

        /**
         * Given a @param[goalQuery] as a function that matches a goal
         * and extracts a context from it if the goal matches.
         * @return a plan builder for goal removal triggers.
         */
        fun <Context : Any> goal(goalQuery: Goal.() -> Context?): PlanBuilder.Removal.Goal<Belief, Goal, Context>
    }

    /**
     * Builder interface for defining triggers based on goal failure interceptions.
     */
    interface FailureInterception<Belief : Any, Goal : Any> : TriggerBuilder<Belief, Goal> {

        /**
         * Given a @param[goalQuery] as a function that matches a goal
         * and extracts a context from it if the goal matches.
         * @return a plan builder for goal failure interception triggers.
         */
        fun <Context : Any> goal(
            goalQuery: Goal.() -> Context?,
        ): PlanBuilder.FailureInterception.Goal<Belief, Goal, Context>
    }
}
