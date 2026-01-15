//package it.unibo.jakta.environment
//
//import it.unibo.jakta.agent.basImpl.AgentID
//import it.unibo.jakta.environment.Movement.Events.Factory.position
//import it.unibo.jakta.environment.Recharging.Events.Factory.chargeLevel
//import it.unibo.jakta.event.Event
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.channels.SendChannel
//import kotlinx.coroutines.delay
//
//interface BrokerEnvironment {
//    val broker: SendChannel<Event.External.Perception>
//}
//
//interface SpatialEnvironment<P> : BrokerEnvironment {
//    val positions: Map<AgentID, P>
//}
//
//interface Skill
//
//interface Recharging : Skill {
//    suspend fun recharge()
//
//    object Events {
//        data class ChargeLevel internal constructor(val level: Int) : Event.External.Perception
//
//        // factory for events of Recharging
//        // using the receiver prevents usage from outside extensions of the Recharging interface
//        object Factory {
//            fun Recharging.chargeLevel(level: Int): ChargeLevel = ChargeLevel(level)
//        }
//    }
//}
//
//interface Movement<P> : Skill {
//    context(s1: Recharging)
//    fun moveTo(newPos: P)
//
//    object Events {
//        data class Position<P> internal constructor(val position: P) : Event.External.Perception
//
//        object Factory {
//            fun <P> Movement<P>.position(pos: P): Position<P> = Position(pos)
//        }
//    }
//}
//
//class FixedTimeRecharging(val e: BrokerEnvironment) : Recharging {
//    override suspend fun recharge() {
//        delay(3000)
//        e.broker.send(chargeLevel(100))
//    }
//}
//
//class GridMovement(val e: GridEnvironment) : Movement<Pair<Int, Int>> {
//    context(s1: Recharging)
//    override fun moveTo(newPos: Pair<Int, Int>) {
//        e.broker.trySend(position(newPos))
//    }
//}
//
//class GridEnvironment : SpatialEnvironment<Pair<Int, Int>> {
//    override val broker: SendChannel<Event.External.Perception> = Channel()
//    override val positions: Map<AgentID, Pair<Int, Int>> = mapOf()
//}
//
//class CustomSkillSet(val e: GridEnvironment) :
//    Recharging by FixedTimeRecharging(e),
//    Movement<Pair<Int, Int>> by GridMovement(e)
//
//context(t: T)
//suspend fun <T> myPlan(block: suspend T.() -> Unit) {
//    t.block()
//}
//
//suspend fun mai22n() {
//    val e = GridEnvironment()
//    val skSet = CustomSkillSet(e)
//
//    context(skSet) {
//        myPlan {
//            recharge()
//            moveTo(1 to 1)
//        }
//    }
//}
//
//
//
//interface A1 {
//    fun a1()
//}
//interface A2 {
//    fun a2()
//}
//
//class CommonClass() : A1, A2 {
//    override fun a1() {
//        println("CommonClass")
//    }
//
//    override fun a2() {
//        println("CommonClass")
//    }
//}
//
//interface Pluan<A: Any> {
//    fun run(a: A)
//}
//
//fun main() {
//
//    val common = CommonClass()
//
//    val plan = object: Pluan<A1> {
//        override fun run(a: A1) {
//            a.a1()
//        }
//    }
//
//    val cast = plan as Pluan<CommonClass>
//
//    cast.run(common)
//
//    println(cast.toString())
//
//}
