package it.unibo.jakta.dsl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.agent.AgentBuilderImpl
import it.unibo.jakta.dsl.mas.BaseMasBuilder
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.plan.BeliefAdditionPlanBuilderImpl
import it.unibo.jakta.dsl.plan.PlanBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.PlanLibraryBuilderImpl
import it.unibo.jakta.dsl.plan.TriggerAdditionImpl
import it.unibo.jakta.dsl.plan.TriggerRemovalImpl
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.Plan

/**
 * DSL entrypoint for creating a Multi-Agent System (MAS).
 * It takes a [NodeBuilder] and a block of code that defines the MAS structure.
 */
@JaktaDSL
fun <N : ExecutableNode<*>, NB : NodeBuilder<*, N>> mas(
    builderFactory: () -> NB,
    block: MasBuilder<N, NB>.() -> Unit,
): MasBuilder<N, NB> = BaseMasBuilder(builderFactory).apply(block)

/**
 * Entry point for creating a node using the JaKtA DSL.
 * @return an instantiated MAS.
 */
fun <Body : Any, N : ExecutableNode<Body>, NB : NodeBuilder<Body, N>> node(
    builderFactory: () -> NB,
    block: NB.() -> Unit,
): ExecutableNode<Body> {
    val builder = builderFactory()
    builder.apply(block)
    return builder.build()
}

/**
 * Entry point for creating an agent using the JaKtA DSL.
 * @return a factory to create an agent, given the node the agent will run on.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Body : Any> agent(
    block: AgentBuilder<Belief, Goal, Body>.() -> Unit,
): (Node<Body>) -> AgentSpecification<Belief, Goal, Body> = { node ->
    val ab = AgentBuilderImpl<Belief, Goal, Body>(node)
    ab.apply(block)
    ab.build()
}

/**
 * Entry point for creating an agent using the JaKtA DSL.
 * @param id the id for the Agent.
 * @return a factory to create an agent, given the node the agent will run on.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Body : Any> agent(
    id: AgentID,
    block: AgentBuilder<Belief, Goal, Body>.() -> Unit,
): (Node<Body>) -> AgentSpecification<Belief, Goal, Body> = { node ->
    val ab = AgentBuilderImpl<Belief, Goal, Body>(node, id)
    ab.apply(block)
    ab.build()
}

// TODO entrypoint for plans???
// this is tricky due to the way the DSL is constructed
// create an entrypoint for a single standalone plan is hard...

// TODO maybe actually make the triggerBuilder implement these interfaces?

/**
 * Entrypoint to define a list of plans for an agent.
 */
@JaktaDSL
fun <Belief : Any, Goal : Any, Body : Any> plans(
    block: PlanLibraryBuilder<Belief, Goal>.(Node<Body>) -> Unit,
): (Node<Body>) -> List<Plan<Belief, Goal, *, *, *>> {
    val plans = mutableListOf<Plan<Belief, Goal, *, *, *>>()
    val libraryBuilder = PlanLibraryBuilderImpl(
        addBeliefPlan = { plans.add(it) },
        addGoalPlan = { plans.add(it) },
    )
    return { node ->
        libraryBuilder.apply { block(node) }
        plans
    }
}

// interface BeliefOnlyAdditionTrigger<Belief : Any, Goal : Any> {
//    /**
//     * Given a @param[beliefQuery] as a function that matches a belief
//     * and extracts a context from it if the belief matches.
//     * @return a plan builder for belief addition triggers.
//     */
//    fun <Context : Any> belief(
//        beliefQuery: Belief.() -> Context?,
//    ): PlanBuilder.Addition.Belief<Belief, Goal, Context>
// }

// /**
// * Entry point for belief removal only plans.
// */
// interface BeliefOnlyRemovalTrigger<Belief : Any, Goal : Any> {
//    /**
//     * Given a @param[beliefQuery] as a function that matches a belief
//     * and extracts a context from it if the belief matches.
//     * @return a plan builder for belief removal triggers.
//     */
//    fun <Context : Any> belief(
//        beliefQuery: Belief.() -> Context?,
//    ): PlanBuilder.Removal.Belief<Belief, Goal, Context>
// }
//
// public class BeliefPlan<Belief : Any, Goal : Any> {
//    val adding: BeliefOnlyAdditionTrigger<Belief, Goal>
//        get() =
//            object : BeliefOnlyAdditionTrigger<Belief, Goal> {
//                val trigger = TriggerAdditionImpl<Belief, Goal>({}, {})
//
//                override fun <Context : Any> belief(
//                    beliefQuery: Belief.() -> Context?,
//                ): PlanBuilder.Addition.Belief<Belief, Goal, Context> = trigger.belief(beliefQuery)
//            }
//
//    val removing: BeliefOnlyRemovalTrigger<Belief, Goal>
//        get() =
//            object : BeliefOnlyRemovalTrigger<Belief, Goal> {
//                val trigger = TriggerRemovalImpl<Belief, Goal>({}, {})
//
//                override fun <Context : Any> belief(
//                    beliefQuery: Belief.() -> Context?,
//                ): PlanBuilder.Removal.Belief<Belief, Goal, Context> = trigger.belief(beliefQuery)
//            }
//
//    companion object {
//        fun <Belief : Any, Goal : Any> of(
//            block: BeliefPlan<Belief, Goal>.() -> Plan.Belief<Belief, Goal, *, *>,
//        ): Plan.Belief<Belief, Goal, *, *> = block(BeliefPlan())
//    }
// }
