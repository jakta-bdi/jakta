package it.unibo.jakta.plans

import it.unibo.jakta.events.Trigger
import it.unibo.tuprolog.core.Struct

interface PrologPlanLibrary : PlanLibrary<
    Trigger<Struct>,
    Guard<Struct>,
    Plan<Trigger<Struct>, Guard<Struct>>,
    > {
    companion object {
        fun of(plans: List<Plan>): PrologPlanLibrary = PlanLibraryImpl(plans)
        fun of(vararg plans: Plan): PlanLibrary = of(plans.asList())

        fun empty(): PlanLibrary = PlanLibraryImpl(emptyList())
    }
}
