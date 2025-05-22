package it.unibo.jakta.actions

interface SideEffect

interface Action : (ActionInvocationContext) -> List<SideEffect>

//action("pluto", 2) {
//    val gg = do_something(argument.1, argument.2)
//    super.side_effect(gg)
//}
//
//agent {
//    believes {
//
//    }
//    goals {
//        - start
//    }
//    plans {
//        whenever(start) then {
//            pluto(pippo, baudo)
//        } causes {
//            newPlan()
//            messageSent()
//        } onFailure {
//
//        }
//    }
//}

