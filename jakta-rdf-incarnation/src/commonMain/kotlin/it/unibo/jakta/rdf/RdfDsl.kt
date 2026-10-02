package it.unibo.jakta.rdf

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.agent.BeliefBuilder
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.PlanScope

/**
 * Adds all the triples of the Turtle [document] to the initial beliefs.
 */
@JaktaDSL
context(scope: RdfScope)
fun BeliefBuilder<RdfBelief>.turtle(document: String) = parseTurtle(document).forEach { +it }

/**
 * Creates an initial goal from a ground Turtle snippet, e.g. `initialGoal("[] a ex:Count ; ex:to 10")`.
 */
context(scope: RdfScope)
fun initialGoal(turtle: String): RdfGoal = parseTurtle(turtle)

/**
 * Creates a goal from a Turtle snippet whose variables are replaced with the terms bound in the plan context.
 */
context(scope: RdfScope, planContext: RdfContext)
fun goal(turtle: String): RdfGoal = parseTurtle(turtle, planContext)

/**
 * Creates a belief from a single-triple Turtle snippet whose variables are replaced
 * with the terms bound in the plan context.
 */
context(scope: RdfScope, planContext: RdfContext)
fun belief(turtle: String): RdfBelief = parseTurtle(turtle, planContext).let { triples ->
    require(triples.size == 1) { "A belief must be a single triple, but got $triples" }
    triples.single()
}

/**
 * Matches the belief against a SPARQL graph [pattern], e.g. `matchingBelief("?who ex:knows ex:bob")`.
 */
@JaktaDSL
context(scope: RdfScope)
fun RdfBelief.matchingBelief(pattern: String): RdfContext? = listOf(this).firstSolution(pattern, RdfContext())

/**
 * Matches the goal graph against a SPARQL graph [pattern], e.g. `matchingGoal("?c a ex:Count ; ex:to ?max")`.
 */
@JaktaDSL
context(scope: RdfScope)
fun RdfGoal.matchingGoal(pattern: String): RdfContext? = firstSolution(pattern, RdfContext())

/**
 * Checks the SPARQL graph [pattern] against the beliefs (and their OWL 2 RL entailments),
 * extending the plan context with the bindings of the first solution.
 * The pattern can refer to variables already bound by the trigger, e.g. `satisfies("FILTER(?n < ?max)")`.
 */
@JaktaDSL
context(scope: RdfScope)
fun GuardScope<RdfBelief, RdfContext>.satisfies(pattern: String): RdfContext? =
    beliefs.withOwlRlEntailment().firstSolution(pattern, context)

/**
 * Like [satisfies], but inside a plan body: fails the plan if the [pattern] has no solution.
 */
context(scope: RdfScope)
fun PlanScope<RdfBelief, RdfGoal, RdfContext>.testQuery(pattern: String) {
    agent.beliefs.withOwlRlEntailment().firstSolution(pattern, context)
        ?: error("The query $pattern is not satisfied")
}

/**
 * Returns all the solutions of the SPARQL graph [pattern] over the beliefs (and their OWL 2 RL entailments),
 * each one as the plan context extended with that solution's bindings.
 */
context(scope: RdfScope)
fun PlanScope<RdfBelief, RdfGoal, RdfContext>.queryAll(pattern: String): List<RdfContext> =
    agent.beliefs.withOwlRlEntailment().solutions(pattern, context).map { RdfContext(context.bindings + it) }

context(scope: RdfScope)
private fun Iterable<RdfBelief>.firstSolution(pattern: String, context: RdfContext): RdfContext? =
    solutions(pattern, context).firstOrNull()?.let {
        context += it
        context
    }
