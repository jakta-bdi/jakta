package it.unibo.jakta.goal

import it.unibo.jakta.belief.ifBeliefMatches
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologGoal = Struct

fun PrologGoal.ifGoalMatches(goal: PrologGoal): Substitution? =
    when (val substitution = this mguWith goal) {
        is Substitution.Fail -> null
        else -> substitution
    }
