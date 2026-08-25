package it.unibo.jakta.logic

import it.unibo.tuprolog.core.Substitution

/**
 * A mutable wrapper for a [Substitution] used as the context of a plan to allow
 * adding new substitutions to the existing one.
 * @property substitution The current [Substitution] in the context.
 */
class MutableSubstitutionPlanContext(var substitution: Substitution) {

    /**
     * Adds the passed substitution to the existing one.
     * @param other the substitution to add.
     */
    operator fun plusAssign(other: Substitution) {
        substitution += other
    }
}
