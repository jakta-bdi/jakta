package it.unibo.jakta.goals

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

fun EmptyGoal<Struct>.applySubstitution(substitution: Substitution): EmptyGoal<Struct> = this

fun AddBelief<PrologBelief>.applySubstitution(substitution: Substitution) =
    AddBelief(value.applySubstitution(substitution))

fun RemoveBelief<PrologBelief>.applySubstitution(substitution: Substitution) =
    RemoveBelief(value.applySubstitution(substitution))

fun UpdateBelief<PrologBelief>.applySubstitution(substitution: Substitution) =
    UpdateBelief(value.applySubstitution(substitution))

fun Achieve<Struct>.applySubstitution(substitution: Substitution) =
    Achieve(value.apply(substitution).castToStruct())

fun Test<Struct>.applySubstitution(substitution: Substitution) =
    Test(value.apply(substitution).castToStruct())

fun Spawn<Struct>.applySubstitution(substitution: Substitution) =
    Spawn(value.apply(substitution).castToStruct())

fun Act<Struct>.applySubstitution(substitution: Substitution) =
    Act(value.apply(substitution).castToStruct())

fun ActInternally<Struct>.applySubstitution(substitution: Substitution) =
    ActInternally(value.apply(substitution).castToStruct())

fun ActExternally<Struct>.applySubstitution(substitution: Substitution) =
    ActExternally(value.apply(substitution).castToStruct())
