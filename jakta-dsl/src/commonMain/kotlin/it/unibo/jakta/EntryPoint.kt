package it.unibo.jakta

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.mas.MAS
import it.unibo.jakta.mas.MasBuilder
import it.unibo.jakta.mas.MasBuilderImpl
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanBuilder
import it.unibo.jakta.plan.TriggerAdditionImpl
import it.unibo.jakta.plan.TriggerRemovalImpl

/**
 * Entry point for creating a multi-agent system using the Jakta DSL.
 * @return an instantiated MAS.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> mas(
    block: MasBuilder<Belief, Goal, Env>.() -> Unit,
): MAS<Belief, Goal, Env> {
    val mb = MasBuilderImpl<Belief, Goal, Env>()
    mb.apply(block)
    return mb.build()
}

/**
 * Entry point for creating an agent using the Jakta DSL.
 * @return an instantiated Agent.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Env : Environment> agent(
    block: AgentBuilder<Belief, Goal, Env>.() -> Unit,
): Agent<Belief, Goal, Env> {
    val ab = AgentBuilderImpl<Belief, Goal, Env>()
    ab.apply(block)
    return ab.build()
}

// TODO entrypoint for plans???
// this is tricky due to the way the DSL is constructed
// create an entrypoint for a single standalone plan is hard...

// TODO this works but it is error prone.
// a user might be tempted to create multiple belief plans in the same block but only the latter is returned

// TODO maybe actually make the triggerBuilder implement these interfaces?

/**
 * Entry point for belief addition only plans.
 */
interface BeliefOnlyAdditionTrigger<Belief : Any, Goal : Any, Env : Environment> {
    /**
     * Given a @param[beliefQuery] as a function that matches a belief
     * and extracts a context from it if the belief matches.
     * @return a plan builder for belief addition triggers.
     */
    fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context>
}

/**
 * Entry point for belief removal only plans.
 */
interface BeliefOnlyRemovalTrigger<Belief : Any, Goal : Any, Env : Environment> {
    /**
     * Given a @param[beliefQuery] as a function that matches a belief
     * and extracts a context from it if the belief matches.
     * @return a plan builder for belief removal triggers.
     */
    fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context>
}

private class BeliefPlan<Belief : Any, Goal : Any, Env : Environment> {
    val adding: BeliefOnlyAdditionTrigger<Belief, Goal, Env>
        get() =
            object : BeliefOnlyAdditionTrigger<Belief, Goal, Env> {
                val trigger = TriggerAdditionImpl<Belief, Goal, Env>({}, {})

                override fun <Context : Any> belief(
                    beliefQuery: Belief.() -> Context?,
                ): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> = trigger.belief(beliefQuery)
            }

    val removing: BeliefOnlyRemovalTrigger<Belief, Goal, Env>
        get() =
            object : BeliefOnlyRemovalTrigger<Belief, Goal, Env> {
                val trigger = TriggerRemovalImpl<Belief, Goal, Env>({}, {})

                override fun <Context : Any> belief(
                    beliefQuery: Belief.() -> Context?,
                ): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> = trigger.belief(beliefQuery)
            }

    companion object {
        fun <Belief : Any, Goal : Any, Env : Environment> of(
            block: BeliefPlan<Belief, Goal, Env>.() -> Plan.Belief<Belief, Goal, Env, *, *>,
        ): Plan.Belief<Belief, Goal, Env, *, *> = block(BeliefPlan())
    }
}
