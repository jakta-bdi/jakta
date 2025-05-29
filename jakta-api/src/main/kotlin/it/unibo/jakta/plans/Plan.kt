package it.unibo.jakta.plans

import it.unibo.jakta.actions.Action
import it.unibo.jakta.events.Event
import kotlin.reflect.KClass

// TODO: sealed
interface Plan<Belief : Any, Query : Any, Result> {
//    val id: PlanID
    val trigger: Query
    val guard: Query
    val actions: List<Action<Belief, Query, Result>>
    val type: Plan.Type

    enum class Type {
        AddBelief(Event.Internal.Belief.Add::class),
        RemoveBelief(Event.Internal.Belief.Remove::class),
        AddGoal(Event.Internal.Goal.Achieve.Add::class),
        RemoveGoal(Event.Internal.Goal.Achieve.Remove::class),
        Test(Event.Internal.Goal.Test.Add::class),
        FailTest(Event.Internal.Goal.Test.Remove::class),
        ;

        private val eventType: KClass<out Event>

        constructor(eventType: KClass<out Event>) {
            this.eventType = eventType
        }

        fun matches(event: Event.Internal): Boolean = eventType.isInstance(event)
    }
}

data class SimplePlan<Belief : Any, Query : Any, Result>(
    override val type: Plan.Type,
    override val trigger: Query,
    override val guard: Query,
    override val actions: List<Action<Belief, Query, Result>>,
) : Plan<Belief, Query, Result>
