package it.unibo.jakta.plans

import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.BeliefBase

interface Plan {
    val tasks: List<Action<*, *>>
}
