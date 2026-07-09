package it.unibo.jakta.kqml

import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.jakta.plan.Plan
import it.unibo.tuprolog.core.Struct

sealed interface KQMLPayload

@Suppress("ClassNaming")
data class tell(val belief: PrologBelief) : KQMLPayload {
    init {
        requireGround(belief) { "The belief to tell must be ground, but got $belief"}
    }
}

@Suppress("ClassNaming")
data class untell(val belief: PrologBelief) : KQMLPayload {
    init {
        requirePredicate(belief) { "The belief to untell must be a predicate, but got $belief"}
    }
}


@Suppress("ClassNaming")
data class achieve(val goal: PrologGoal) : KQMLPayload {
    init {
        requireGround(goal) { "The goal to achieve must be ground, but got $goal"}
    }
}


@Suppress("ClassNaming")
data class unachieve(val goal: PrologGoal) : KQMLPayload {
    init {
        requirePredicate(goal) { "The goal to unachieve must be a predicate, but got $goal"}
    }
}

// TODO these require a suspending semantic for the "send" operation,
//  which we currently are unable to support


//@Suppress("ClassNaming")
//data class askOne(val query: Struct) : KQMLPayload {
//    init {
//        requirePredicate(query) { "The query to askOne must be a predicate, but got $query" }
//    }
//}
//
//@Suppress("ClassNaming")
//data class askAll(val query: Struct) : KQMLPayload {
//    init {
//        requirePredicate(query) { "The query to askAll must be a predicate, but got $query" }
//    }
//}

// TODO these require the ability to share plans.
//  for now these are only placeholders

//@Suppress("ClassNaming")
//data class tellHow<TriggerEntity : Any, Context : Any, PlanResult>(
//    val plan: Plan<PrologBelief, PrologGoal,TriggerEntity, Context, PlanResult>
//    ) : KQMLPayload
//
//@Suppress("ClassNaming")
//data class untellHow<TriggerEntity : Any, Context : Any, PlanResult>(
//    val plan: Plan<PrologBelief, PrologGoal,TriggerEntity, Context, PlanResult>
//    ) : KQMLPayload
//
//// TODO not sure if this is the best way to do this
//@Suppress("ClassNaming")
//data class askHowGoal<PlanResult>(val trigger: AgentEvent.Internal.Goal<PrologGoal, PlanResult>) : KQMLPayload
//
//@Suppress("ClassNaming")
//data class askHowBelief(val trigger: AgentEvent.Internal.Belief<PrologBelief>) : KQMLPayload
