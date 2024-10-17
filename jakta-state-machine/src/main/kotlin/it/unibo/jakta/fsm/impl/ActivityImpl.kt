package it.unibo.jakta.fsm.impl

import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Activity.Controller

internal data class ActivityImpl(
    val onBeginProcedure: (controller: Controller) -> Unit = {},
    val onStepProcedure: (controller: Controller) -> Unit = {},
    val onEndProcedure: (controller: Controller) -> Unit = {},
) : Activity {
    override fun onBegin(controller: Controller) = onBeginProcedure(controller)

    override fun onStep(controller: Controller) = onStepProcedure(controller)

    override fun onEnd(controller: Controller) = onEndProcedure(controller)
}
