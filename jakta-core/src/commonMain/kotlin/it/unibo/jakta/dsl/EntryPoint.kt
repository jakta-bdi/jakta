package it.unibo.jakta.dsl

import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.agent.AgentBuilderImpl
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node

/**
 * Entry point for creating a node using the JaKtA DSL.
 * @return an instantiated MAS.
 */
fun <Body : Any, N : ExecutableNode<Body>, NB : NodeBuilder<Body, N>> node(
    builder: NB,
    block: NB.() -> Unit,
): ExecutableNode<Body> {
    builder.apply(block)
    return builder.build()
}

/**
 * Entry point for creating an agent using the JaKtA DSL.
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

// TODO maybe actually make the triggerBuilder implement these interfaces?

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
//
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
