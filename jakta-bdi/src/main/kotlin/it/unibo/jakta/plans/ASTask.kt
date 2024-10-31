package it.unibo.jakta.plans

import it.unibo.tuprolog.core.Substitution

interface ASTask<Result: ExecutionResult, out SelfType: Task<Result>> {
    fun applySubstitution(substitution: Substitution): SelfType
}