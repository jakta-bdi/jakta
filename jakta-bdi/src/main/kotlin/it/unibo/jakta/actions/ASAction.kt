package it.unibo.jakta.actions

import it.unibo.tuprolog.core.Substitution

interface ASAction : Action {
    fun applySubstitution(substitution: Substitution): ASAction

    fun addResults(substitution: Substitution)
}
