package it.unibo.jakta.dsl.belief

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologBelief = Rule

private fun PrologBelief.matchBelief(belief: Fact): Substitution? = when (val substitution = this mguWith belief) {
    is Substitution.Fail -> null
    else -> substitution
}

/**
 * Extension function to match a Prolog belief against a belief query defined in the provided block.
 * @param block A lambda function that defines the belief query to be matched against the current belief
 * * @return The matched [Substitution] if the belief matches the query, or null if it does not match.
 */
@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun PrologBelief.matching(block: JaktaLogicProgrammingScope.() -> Struct): Substitution? =
    matchBelief(contextualBeliefQuery(block))

/**
 * Creates an initial Prolog belief from the provided block, ensuring that it is a predicate and ground.
 * @param block A lambda function that defines the initial belief to be created.
 * @return The created [Fact] if it is a valid predicate and ground.
 */
fun initialBelief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    JaktaLogicProgrammingScope().block().also { struct ->
        requirePredicate(struct) { "Belief must be a predicate, but got $it" }
        requireGround(struct) { "Belief must be ground, but got $it" }
    },
)

/**
 * Creates a Prolog belief from the provided block, applying the given substitution
 * and ensuring that it is a predicate and ground.
 * @param block A lambda function that defines the belief to be created.
 * @param substitution The substitution to be applied to the belief.
 * @return The created [Fact] if it is a valid predicate and ground.
 */
context(scope: JaktaLogicProgrammingScope, substitution: Substitution)
fun belief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    (scope.block().apply(substitution) as Struct).also { struct ->
        requirePredicate(struct) { "Belief must be a predicate, but got $it" }
        requireGround(struct) { "Belief must be ground, but got $it" }
    },
)

/**
 * Creates a Prolog inference rule from the provided block.
 * @param block A lambda function that defines the inference rule to be created.
 * @return The created [Rule] if it is a valid rule.
 */
fun inferenceRule(block: JaktaLogicProgrammingScope.() -> Rule): Rule = JaktaLogicProgrammingScope().block()

/**
 * Creates a Prolog belief query from the provided block, ensuring that it is a predicate.
 * @param block A lambda function that defines the belief query to be created.
 * @return The created [Fact] as a query if it is a valid predicate.
 */
//TODO check if this is correct, maybe it should be a Struct instead of a Fact
fun beliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    JaktaLogicProgrammingScope().block().also { struct ->
        requirePredicate(struct) { "Belief query must be a predicate, but got $it" }
    },
)

context(scope: JaktaLogicProgrammingScope)
private fun contextualBeliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    scope.block().also { struct ->
        requirePredicate(struct) { "Belief query must be a predicate, but got $it" }
    },
)
