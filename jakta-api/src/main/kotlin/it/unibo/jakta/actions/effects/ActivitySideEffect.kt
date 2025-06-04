//package it.unibo.jakta.actions.effects
//
//import it.unibo.jakta.fsm.Activity
//
//fun interface ActivitySideEffect<Belief : Any, Query : Any, Result> :
//    SideEffect<Belief, Query, Result>,
//    (Activity.Controller) -> Unit
//
//data class Sleep<Belief : Any, Query : Any, Result>(val millis: Long) : ActivitySideEffect {
//    override fun invoke(controller: Activity.Controller) {
//        controller.sleep(millis)
//    }
//}
//
//object Stop<Belief : Any, Query : Any, Result> : ActivitySideEffect {
//    override fun invoke(controller: Activity.Controller) {
//        controller.stop()
//    }
//}
//
//object Pause : ActivitySideEffect {
//    override fun invoke(controller: Activity.Controller) {
//        controller.pause()
//    }
//}
