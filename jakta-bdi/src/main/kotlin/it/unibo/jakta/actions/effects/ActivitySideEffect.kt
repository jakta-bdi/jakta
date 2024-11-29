package it.unibo.jakta.actions.effects

import it.unibo.jakta.fsm.Activity

fun interface ActivitySideEffect : ActionSideEffect, (Activity.Controller) -> Unit

class Sleep(val millis: Long) : ActivitySideEffect {
    override fun invoke(controller: Activity.Controller) {
        controller.sleep(millis)
    }
}

object Stop : ActivitySideEffect {
    override fun invoke(controller: Activity.Controller) {
        controller.stop()
    }
}

object Pause : ActivitySideEffect {
    override fun invoke(controller: Activity.Controller) {
        controller.pause()
    }
}
