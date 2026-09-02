package it.unibo.jakta.kqml

import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import kotlin.uuid.Uuid

/**
 * Tag interface for all KQML payloads.
 */
sealed class KQMLPayload {
    /**
     * A unique id of the message.
     */
    val id: Uuid = Uuid.random()
}

/**
 * KQML payload for telling a beliefQuery to an agent.
 * The [beliefs] must be ground. [replyingTo] is an optional id of the message this tell message is replying to.
 */
data class Tell(val beliefs: Set<PrologBelief>, val replyingTo: Uuid? = null) : KQMLPayload() {
    init {
        beliefs.all { it is Fact } || error { "All beliefs to tell must be facts, but got $beliefs" }
        beliefs.forEach { b ->
            requirePredicate(b.head) { "All beliefs to tell must be a predicate, but got $b" }
            requireGround(b) { "All beliefs to tell must be ground, but got $it" }
        }
    }
}

/**
 * KQML payload for untelling a beliefQuery to an agent.
 * The [beliefQuery] must be a predicate.
 */
data class Untell(val beliefQuery: Struct) : KQMLPayload() {
    init {
        requirePredicate(beliefQuery) { "The beliefQuery to untell must be a predicate, but got $beliefQuery" }
    }
}

/**
 * KQML payload to delegate a goal to an agent.
 * The [goal] must be ground.
 */
data class Achieve(val goal: PrologGoal) : KQMLPayload() {
    init {
        requirePredicate(goal) { "The goal to achieve must be a predicate, but got $goal" }
        requireGround(goal) { "The goal to achieve must be ground, but got $goal" }
    }
}


//TODO rename in Drop or DropGoal?
/**
 * KQML payload for telling an agent to stop pursuing a (delegated) goal.
 * The [goalQuery] must be a predicate.
 */
data class Unachieve(val goalQuery: Struct) : KQMLPayload() {
    init {
        requirePredicate(goalQuery) { "The goal to unachieve must be a predicate, but got $goalQuery" }
    }
}

/**
 * KQML payload for asking an agent to reply with the first beliefQuery that satisfies the given query.
 * @param query the query to satisfy.
 */
data class AskOne(val query: Struct) : KQMLPayload() {
    init {
        requirePredicate(query) { "The query to askOne must be a predicate, but got $query" }
    }
}

/**
 * KQML payload for asking an agent to reply with all the beliefs that satisfy the given query.
 * @param query the query to satisfy.
 */
data class AskAll(val query: Struct) : KQMLPayload() {
    init {
        requirePredicate(query) { "The query to askAll must be a predicate, but got $query" }
    }
}

// TODO these require the ability to share plans.
//  for now these are only placeholders as plans cannot be serialized

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
