package it.unibo.jakta.events

import it.unibo.jakta.Intention
import it.unibo.jakta.events.Event.Internal.Goal

sealed interface Event {

    sealed interface Internal : Event {
        sealed interface Goal<Belief : Any, Query : Any, Result> : Internal {
            val intention: Intention<Belief, Query, Result>?

            sealed interface Test<Belief : Any, Query : Any, Result> : Goal<Belief, Query, Result> {
                val query: Query

                interface Add<Belief : Any, Query : Any, Result> : Test<Belief, Query, Result>
                interface Remove<Belief : Any, Query : Any, Result> : Test<Belief, Query, Result>
            }

            sealed interface Achieve<Belief : Any, Query : Any, Result, GoalType> : Goal<Belief, Query, Result> {
                val goal: GoalType

                interface Add<Belief : Any, Query : Any, Result, GoalType> : Achieve<Belief, Query, Result, GoalType>
                interface Remove<Belief : Any, Query : Any, Result, GoalType> : Achieve<Belief, Query, Result, GoalType>
            }
        }
        sealed interface Belief<out B : Any> : Internal {
            val belief: B
            interface Add<out B : Any> : Belief<B>
            interface Remove<out B : Any> : Belief<B>
        }
    }
    interface External : Event
}
