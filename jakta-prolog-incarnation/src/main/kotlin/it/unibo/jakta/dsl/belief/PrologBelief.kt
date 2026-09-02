package it.unibo.jakta.dsl.belief

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

typealias PrologBelief = Rule

fun PrologBelief.matchBelief(beliefQuery: Struct): MutableSubstitutionPlanContext? = when (
    val substitution = this.annotatedMguWith(beliefQuery)
) {
    is Substitution.Fail -> null
    else -> MutableSubstitutionPlanContext(substitution)
}

/**
 * Extension function to match a Prolog beliefQuery against a beliefQuery query defined in the provided block.
 * @param block A lambda function that defines the beliefQuery query to be matched against the current beliefQuery
 * @return The matched [MutableSubstitutionPlanContext] if the beliefQuery matches the query, or null if it does not match.
 */
@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun PrologBelief.matchingBelief(block: JaktaLogicProgrammingScope.() -> Struct): MutableSubstitutionPlanContext? =
    matchBelief(noSubstitutionBeliefQuery(block))


/**
 * Creates an initial Prolog beliefQuery from the provided block, ensuring that it is a predicate and ground.
 * @param block A lambda function that defines the initial beliefQuery to be created.
 * @return The created [Fact] if it is a valid predicate and ground.
 */
fun initialBelief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    JaktaLogicProgrammingScope().block().also { struct ->
        requirePredicate(struct) { "Belief must be a predicate, but got $it" }
        requireGround(struct) { "Belief must be ground, but got $it" }
    },
)

/**
 * Creates a Prolog beliefQuery from the provided block, applying the given substitution
 * and ensuring that it is a predicate and ground.
 * @param block A lambda function that defines the beliefQuery to be created.
 * @param planContext the current context of the plan, containing the substitution of variables in scope.
 * @return The created [Fact] if it is a valid predicate and ground.
 */
context(scope: JaktaLogicProgrammingScope, planContext: MutableSubstitutionPlanContext)
fun belief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    (scope.block().apply(planContext.substitution) as Struct).also { struct ->
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
 * Creates a Prolog beliefQuery query from the provided block, using the contextual [scope].
 * @param block A lambda function that defines the beliefQuery query to be created.
 * @return The created [Fact] as a query if it is a valid predicate.
 */
context(scope: JaktaLogicProgrammingScope, planContext: MutableSubstitutionPlanContext)
fun beliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Struct =
    (scope.block().apply(planContext.substitution) as Struct).also { struct ->
        requirePredicate(struct) { "Belief query must be a predicate, but got $it" }
    }

/**
 * Creates a Prolog beliefQuery query from the provided block, using the contextual [scope].
 * @param block A lambda function that defines the beliefQuery query to be created.
 * @return The created [Fact] as a query if it is a valid predicate.
 */
context(scope: JaktaLogicProgrammingScope)
fun noSubstitutionBeliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Struct =
    scope.block().also { struct ->
        requirePredicate(struct) { "Belief query must be a predicate, but got $it" }
    }

/**
 * Creates a Prolog beliefQuery query using a brand-new scope.
 * @param block A lambda function that defines the beliefQuery query to be created.
 * @return The created [Fact] as a query if it is a valid predicate.
 */
fun newContextBeliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Struct =
    JaktaLogicProgrammingScope().block().also { struct ->
        requirePredicate(struct) { "Belief query must be a predicate, but got $it" }
    }
