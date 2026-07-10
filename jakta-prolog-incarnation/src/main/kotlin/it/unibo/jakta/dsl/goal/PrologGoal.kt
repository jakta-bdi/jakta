package it.unibo.jakta.dsl.goal

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologGoal = Struct

private fun PrologGoal.matchGoal(goal: PrologGoal): Substitution? = when (val substitution = this mguWith goal) {
    is Substitution.Fail -> null
    else -> substitution
}

/**
 * Extension function to match a Prolog goal against a goal query defined in the provided block.
 * @param block A lambda function that defines the goal query to be matched against the current goal.
 * @return The matched [Substitution] if the goal matches the query, or null if it does not match.
 */
@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun PrologGoal.matching(block: JaktaLogicProgrammingScope.() -> Struct): Substitution? = matchGoal(goalQuery(block))

/**
 * Creates an initial Prolog goal from the provided block, ensuring that it is a predicate and ground.
 * @param block A lambda function that defines the initial goal to be created.
 * @return The created [PrologGoal] if it is a valid predicate and ground.
 */
fun initialGoal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    JaktaLogicProgrammingScope().block().also { struct ->
        requirePredicate(struct) { "Initial goal must be a predicate, but got $it" }
        requireGround(struct) { "Goal must be ground, but got $it" }
    }

/**
 * Creates a Prolog goal from the provided block, applying the given substitution
 * and ensuring that it is a predicate and ground.
 * @param block A lambda function that defines the goal to be created.
 * @param substitution The substitution to be applied to the goal.
 * @return The created [PrologGoal] if it is a valid predicate and ground.
 */
context(scope: JaktaLogicProgrammingScope, substitution: Substitution)
fun goal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    (scope.block().apply(substitution) as Struct).also { struct ->
        requirePredicate(struct) { "Goal must be a predicate, but got $it" }
        requireGround(struct) { "Goal must be ground, but got $it" }
    }

/**
 * Creates a Prolog goal query from the provided block, ensuring that it is a predicate.
 * @param block A lambda function that defines the goal query to be created.
 * @return The created [PrologGoal] if it is a valid predicate,
 */
context(scope: JaktaLogicProgrammingScope)
private fun goalQuery(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal = scope.block().also { struct ->
    requirePredicate(struct) { "Goal query must be a predicate, but got $it" }
}

// TODO can we make an utility for test goals?
//  sometimes it would still be nice to have a way to say
//  "test this condition otherwise achieve this goal"
