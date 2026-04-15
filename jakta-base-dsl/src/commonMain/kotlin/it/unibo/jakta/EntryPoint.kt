package it.unibo.jakta

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanBuilder
import it.unibo.jakta.plan.TriggerAdditionImpl
import it.unibo.jakta.plan.TriggerRemovalImpl

// TODO For now let's focus on the FULL DSL syntax with only the main entrypoint,
// then we will add more features to isolate components...

/**
 * Entry point for creating a multi-agent system using the Jakta DSL.
 * @return an instantiated MAS.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Body : Any> node(block: LocalNodeBuilder<Belief, Goal, Body>.() -> Unit): Node<Body> {
    val nodeBuilder = LocalNodeBuilder<Belief, Goal, Body>()
    nodeBuilder.apply(block)
    return nodeBuilder.build()
} // TODO now this is bound to local

/**
 * Entry point for creating an agent using the Jakta DSL.
 * @return an instantiated Agent.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Body : Any> agent(
    node: Node<Body>,
    block: AgentBuilder<Belief, Goal, Body>.() -> Unit,
): AgentSpecification<Belief, Goal, Body> {
    val ab = AgentBuilderImpl<Belief, Goal, Body>()
    ab.apply(block)
    return ab.build(node)
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
interface BeliefOnlyAdditionTrigger<Belief : Any, Goal : Any> {
    /**
     * Given a @param[beliefQuery] as a function that matches a belief
     * and extracts a context from it if the belief matches.
     * @return a plan builder for belief addition triggers.
     */
    fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Addition.Belief<Belief, Goal, Context>
}

/**
 * Entry point for belief removal only plans.
 */
interface BeliefOnlyRemovalTrigger<Belief : Any, Goal : Any> {
    /**
     * Given a @param[beliefQuery] as a function that matches a belief
     * and extracts a context from it if the belief matches.
     * @return a plan builder for belief removal triggers.
     */
    fun <Context : Any> belief(beliefQuery: Belief.() -> Context?): PlanBuilder.Removal.Belief<Belief, Goal, Context>
}

private class BeliefPlan<Belief : Any, Goal : Any> {
    val adding: BeliefOnlyAdditionTrigger<Belief, Goal>
        get() =
            object : BeliefOnlyAdditionTrigger<Belief, Goal> {
                val trigger = TriggerAdditionImpl<Belief, Goal>({}, {})

                override fun <Context : Any> belief(
                    beliefQuery: Belief.() -> Context?,
                ): PlanBuilder.Addition.Belief<Belief, Goal, Context> = trigger.belief(beliefQuery)
            }

    val removing: BeliefOnlyRemovalTrigger<Belief, Goal>
        get() =
            object : BeliefOnlyRemovalTrigger<Belief, Goal> {
                val trigger = TriggerRemovalImpl<Belief, Goal>({}, {})

                override fun <Context : Any> belief(
                    beliefQuery: Belief.() -> Context?,
                ): PlanBuilder.Removal.Belief<Belief, Goal, Context> = trigger.belief(beliefQuery)
            }

    companion object {
        fun <Belief : Any, Goal : Any> of(
            block: BeliefPlan<Belief, Goal>.() -> Plan.Belief<Belief, Goal, *, *>,
        ): Plan.Belief<Belief, Goal, *, *> = block(BeliefPlan())
    }
}
