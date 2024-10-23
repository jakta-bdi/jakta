package it.unibo.jakta.goals

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

fun EmptyGoal<Struct>.applySubstitution(substitution: Substitution): EmptyGoal<Struct> = this
