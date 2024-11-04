package it.unibo.jakta.plans

import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution

interface ASTask<Result: ExecutionResult, out SelfType: Task<Result>> {
    fun applySubstitution(substitution: Substitution): SelfType

    var currentIntentionProvider: () -> ASIntention
    var agentContextProvider: () -> ASMutableAgentContext
    var environmentProvider: () -> Environment
}