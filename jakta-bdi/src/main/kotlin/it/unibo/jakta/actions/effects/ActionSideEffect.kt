package it.unibo.jakta.actions.effects

import it.unibo.jakta.fsm.Activity

interface ActionSideEffect {
    operator fun invoke()
}
