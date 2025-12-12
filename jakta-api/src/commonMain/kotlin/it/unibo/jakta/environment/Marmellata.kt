//package it.unibo.jakta.environment
//
//import it.unibo.jakta.environment.Movement.Events.Factory.position
//import it.unibo.jakta.environment.Recharging.Events.Factory.chargeLevel
//import it.unibo.jakta.event.Event
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
//import kotlinx.coroutines.channels.SendChannel
//import kotlinx.coroutines.delay
//
////This will become the actual Environment interface
//interface BrokerEnvironment : Environment {
//    val broker : SendChannel<Event.External.Perception>
//}
//
//// an environment with some notion of spatial location
//interface SpatialEnvironment<Position> : BrokerEnvironment {
//    val position: Position
//}
//
////concrete implementation of an environment with spatial nature as int coordinates
//data class GridEnvironment(override var position: Pair<Int, Int>)
//    : SpatialEnvironment<Pair<Int, Int>> {
//    override val broker : SendChannel<Event.External.Perception> = Channel(UNLIMITED)
//}
//
////Interface for skills, skills depend on an environment to be able to send perceptions
////and possibly interact with environment-specific features
//interface Skill<Env: Environment> {
//    val environment: Env
//}
//
////This is a skill that depends on a generic environment, i.e. it does not need any specific feature
////apart from the broker to notify the agent of charge complete
////Lets the agent recharge its battery
//interface Recharging : Skill<BrokerEnvironment> {
//    suspend fun recharge()
//
//    // in this namespace we define all events the skill can generate
//    object Events {
//        data class ChargeLevel internal constructor(val level: Int) :
//            Event.External.Perception
//
//        //factory for events of Recharging
//        //using the receiver prevents usage from outside extensions of the Recharging interface
//        object Factory {
//            fun Recharging.chargeLevel(level: Int): ChargeLevel =
//                ChargeLevel(level)
//        }
//    }
//}
//
//
//
////Implementation of the actual skill for recharging
//data class FixedTimeRecharging(override val environment: BrokerEnvironment) : Recharging {
//    override suspend fun recharge() {
//        println("Recharging...")
//        delay(3000)
//        println("Recharged!")
//        environment.broker.trySend(chargeLevel(100)) // I can use here the factory, but not elsewhere
//    }
//}
//
////This is a generic movement skill that depends on an environment with some spatial nature
//interface Movement<P> : Skill<SpatialEnvironment<P>> {
//    fun moveTo(newPosition: P) : P
//
//    object Events {
//        data class Position<P> internal constructor (val position: P) :
//            Event.External.Perception
//
//        object Factory {
//            fun <P> Movement<P>.position(pos: P): Position<P> =
//                Position(pos)
//        }
//    }
//}
//
////Concrete implementation of the movement skill for a grid environment
//data class GridMovement(override val environment: GridEnvironment) : Movement<Pair<Int, Int>> {
//    override fun moveTo(newPosition: Pair<Int, Int>) : Pair<Int, Int> {
//        environment.position = newPosition
//        environment.broker.trySend(position(newPosition)) //notify the position!
//        return newPosition
//    }
//}
//
//// TODO se uso il context non mi serve passare fare cake pattern con i delegate?
////object SkillSet :
////    Recharging by FixedTimeRecharging(myEnv),
////    Movement<Pair<Int, Int>> by GridMovement(myEnv) {
////    override val environment: GridEnvironment = myEnv
////}
//
//
////TODO dani dice che usando KSP forse questi metodi qua del DSL si possono autogenerare
//// con tutte le possibili combinazioni di contesto che si trovano nel classpath (es. tutte le classi con annotazione @Skill)
//// https://kotlinlang.org/docs/ksp-overview.html
//context(movement: M, recharging: R)
//suspend fun <M: Movement<Pair<Int, Int>> , R : Recharging> agent() {
//    movement.moveTo(0 to 1)
//    recharging.recharge()
//}
//
////TODO using generics allows to generate multiple overloads without changing the name of the method "agent"
//// just like `context` is implemented..
//context(movement: M, recharging: R, skill: S)
//suspend fun <M: Movement<Pair<Int, Int>> , R : Recharging, S: Skill<Environment>> agent() {
//    movement.moveTo(0 to 1)
//    recharging.recharge()
//}
//
//
//suspend fun main() {
//    val myEnv = GridEnvironment(0 to 0 )
//    context(
//        GridMovement(myEnv),
//        FixedTimeRecharging(myEnv)
//    ) {
//        agent()
//    }
//}
