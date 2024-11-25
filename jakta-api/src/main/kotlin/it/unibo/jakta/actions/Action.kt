package it.unibo.jakta.actions

import it.unibo.jakta.plans.ExecutionResult
import it.unibo.jakta.plans.Task

interface Action<Query: Any, Belief, in Argument, Result: ExecutionResult> : Task<Query, Belief, Argument, Result> {
    override suspend fun execute(argument: Argument): Result
}