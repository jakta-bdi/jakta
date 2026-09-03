package it.unibo.jakta.dsl.goal

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

typealias PrologGoal = Struct

private fun PrologGoal.matchGoal(goalQuery: Struct): MutableSubstitutionPlanContext? = when (
    val substitution = this.annotatedMguWith(goalQuery)
) {
    is Substitution.Fail -> null
    else -> MutableSubstitutionPlanContext(substitution)
}

/**
 * Extension function to match a Prolog goal against a goal query defined in the provided block.
 * @param block A lambda function that defines the goal query to be matched against the current goal.
 * @return The matched [MutableSubstitutionPlanContext] if the goal matches the query, or null if it does not match.
 */
@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun PrologGoal.matchingGoal(block: JaktaLogicProgrammingScope.() -> Struct): MutableSubstitutionPlanContext? =
    matchGoal(goalQuery(block))

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
 * @param planContext the current context of the plan, containing the substitution of variables in scope.
 * @return The created [PrologGoal] if it is a valid predicate and ground.
 */
context(scope: JaktaLogicProgrammingScope, planContext: MutableSubstitutionPlanContext)
fun goal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    (scope.block().apply(planContext.substitution) as Struct).also { struct ->
        requirePredicate(struct) { "Goal must be a predicate, but got $it" }
        requireGround(struct) { "Goal must be ground, but got $it" }
    }

/**
 * Creates a Prolog goal query from the provided block, ensuring that it is a predicate.
 * @param block A lambda function that defines the goal query to be created.
 * @return The created [PrologGoal] if it is a valid predicate,
 */
context(scope: JaktaLogicProgrammingScope)
fun goalQuery(block: JaktaLogicProgrammingScope.() -> Struct): Struct = scope.block().also { struct ->
    requirePredicate(struct) { "Goal query must be a predicate, but got $it" }
}

/**
 * Creates a special goal that can be used to write plans that reply to askOne messages received by the agent.
 * @param messageId an optional message id to reply to. If not provided, the goal will match any received message.
 * @param block A lambda function that defines the query to match in this reply.
 * @return the created [PrologGoal].
 */
fun JaktaLogicProgrammingScope.replyOne(query: Term, messageId: Term = Var.anonymous()): PrologGoal = with(this) {
    return "replyOne"(query, messageId)
}

/**
 * Creates a special goal that can be used to write plans that reply to askAll messages received by the agent.
 * @param messageId an optional message id to reply to. If not provided, the goal will match any received message.
 * @param block A lambda function that defines the query to match in this reply.
 * @return the created [PrologGoal].
 */
fun JaktaLogicProgrammingScope.replyAllTo(query: Term, messageId: Term = Var.anonymous()): PrologGoal = with(this) {
    return "replyAllTo"(query, messageId)
}

// TODO can we make an utility for test goals?
//  sometimes it would still be nice to have a way to say
//  "test this condition otherwise achieve this goal"
