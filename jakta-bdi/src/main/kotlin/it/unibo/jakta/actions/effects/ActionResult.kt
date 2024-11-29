package it.unibo.jakta.actions.effects

import it.unibo.jakta.fsm.Activity

interface ActionResult<in Context> {
    fun Context.apply(controller: Activity.Controller?)
}
