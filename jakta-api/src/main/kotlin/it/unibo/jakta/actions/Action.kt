package it.unibo.jakta.actions

import it.unibo.jakta.plans.ExecutionResult

interface Action<in Arguments, SideEffect, Result: ExecutionResult<SideEffect>> {
    suspend fun execute(argument: Arguments): Result
}
