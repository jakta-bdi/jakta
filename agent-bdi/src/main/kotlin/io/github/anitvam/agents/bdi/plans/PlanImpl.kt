package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.ActivationRecord
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override val id: PlanID,
    override val event: Trigger,
    override val guard: Struct,
    override val goals: List<Goal>
) : Plan {
    override fun isApplicable(event: Event, beliefBase: BeliefBase): Boolean {
        val mgu = event.trigger.value mguWith this.event.value
        val actualGuard = guard.apply(mgu).castToStruct()
        return Solver.prolog.solverOf(staticKb = Theory.of(beliefBase)).solveOnce(actualGuard).isYes
    }

    override fun toActivationRecord(): ActivationRecord {
        TODO("Not yet implemented")
    }

}