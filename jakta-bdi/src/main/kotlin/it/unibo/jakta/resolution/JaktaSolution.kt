package it.unibo.jakta.resolution

import it.unibo.tuprolog.solve.Solution as TuprologSolution

data class JaktaSolution(
    override val result: TuprologSolution,
) : Solution<TuprologSolution> {
    override val isSuccess: Boolean
        get() = result.isYes
}

fun TuprologSolution.toJaktaSolution() = JaktaSolution(this)
