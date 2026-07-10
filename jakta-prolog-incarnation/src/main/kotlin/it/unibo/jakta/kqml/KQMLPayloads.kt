package it.unibo.jakta.kqml

import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule

/**
 * Tag interface for all KQML payloads.
 */
sealed interface KQMLPayload

/**
 * KQML payload for telling a belief to an agent.
 * The [belief] must be ground.
 */
data class Tell(val belief: PrologBelief) : KQMLPayload {
    init {
        when (belief) {
            is Fact -> requireGround(belief) { "The belief to tell must be ground, but got $belief" }
        }
    }
}

/**
 * KQML payload for untelling a belief to an agent.
 * The [belief] must be a predicate.
 */
@Suppress("ClassNaming")
data class Untell(val belief: PrologBelief) : KQMLPayload {
    init {
        requirePredicate(belief) { "The belief to untell must be a predicate, but got $belief" }
    }
}

/**
 * KQML payload to delegate a goal to an agent.
 * The [goal] must be ground.
 */
@Suppress("ClassNaming")
data class Achieve(val goal: PrologGoal) : KQMLPayload {
    init {
        requireGround(goal) { "The goal to achieve must be ground, but got $goal" }
    }
}

/**
 * KQML payload for tell an agent to stop pursuing a (delegated) goal.
 * The [goal] must be a predicate.
 */
@Suppress("ClassNaming")
data class Unachieve(val goal: PrologGoal) : KQMLPayload {
    init {
        requirePredicate(goal) { "The goal to unachieve must be a predicate, but got $goal" }
    }
}

// TODO these require a suspending semantic for the "send" operation,
//  which we currently are unable to support

// @Suppress("ClassNaming")
// data class askOne(val query: Struct) : KQMLPayload {
//    init {
//        requirePredicate(query) { "The query to askOne must be a predicate, but got $query" }
//    }
// }
//
// @Suppress("ClassNaming")
// data class askAll(val query: Struct) : KQMLPayload {
//    init {
//        requirePredicate(query) { "The query to askAll must be a predicate, but got $query" }
//    }
// }

// TODO these require the ability to share plans.
//  for now these are only placeholders

// @Suppress("ClassNaming")
// data class tellHow<TriggerEntity : Any, Context : Any, PlanResult>(
//    val plan: Plan<PrologBelief, PrologGoal,TriggerEntity, Context, PlanResult>
//    ) : KQMLPayload
//
// @Suppress("ClassNaming")
// data class untellHow<TriggerEntity : Any, Context : Any, PlanResult>(
//    val plan: Plan<PrologBelief, PrologGoal,TriggerEntity, Context, PlanResult>
//    ) : KQMLPayload
//
// // TODO not sure if this is the best way to do this
// @Suppress("ClassNaming")
// data class askHowGoal<PlanResult>(val trigger: AgentEvent.Internal.Goal<PrologGoal, PlanResult>) : KQMLPayload
//
// @Suppress("ClassNaming")
// data class askHowBelief(val trigger: AgentEvent.Internal.Belief<PrologBelief>) : KQMLPayload
