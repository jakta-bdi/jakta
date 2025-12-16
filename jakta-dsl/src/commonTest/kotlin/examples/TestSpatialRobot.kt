package examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import examples.Movement.Events.Factory.position
import examples.Recharging.Events.Factory.chargeLevel
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.environment.BaseEnvironment
import it.unibo.jakta.event.Event
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlinx.coroutines.delay


open class SpatialEnvironment<Belief: Any, Goal: Any, P> : BaseEnvironment<Belief, Goal>() {
    val positions: Map<AgentID, P> = emptyMap()
}

interface Skill

interface Recharging : Skill {
    suspend fun recharge()

    object Events {
        data class ChargeLevel internal constructor(val level: Int) : Event.External.Perception

        // factory for events of Recharging
        // using the receiver prevents usage from outside extensions of the Recharging interface
        object Factory {
            fun Recharging.chargeLevel(level: Int): ChargeLevel = ChargeLevel(level)
        }
    }
}

interface Movement<P> : Skill {
    context(s1: Recharging)
    fun moveTo(newPos: P)

    object Events {
        data class Position<P> internal constructor(val position: P) : Event.External.Perception

        object Factory {
            fun <P> Movement<P>.position(pos: P): Position<P> = Position(pos)
        }
    }
}

class FixedTimeRecharging<Belief: Any, Goal: Any>(val e: BaseEnvironment<Belief, Goal>) : Recharging {
    override suspend fun recharge() {
        delay(3000)
        e.send(chargeLevel(100))
    }
}

class GridMovement<Belief: Any, Goal: Any>(val e: GridEnvironment<Belief, Goal>) : Movement<Pair<Int, Int>> {
    context(s1: Recharging)
    override fun moveTo(newPos: Pair<Int, Int>) {
        e.trySend(position(newPos))
    }
}

class GridEnvironment<Belief: Any, Goal: Any> : SpatialEnvironment<Belief, Goal, Pair<Int, Int>>()

class CustomSkillSet<Belief: Any, Goal: Any>(val e: GridEnvironment<Belief, Goal>) :
    Recharging by FixedTimeRecharging(e),
    Movement<Pair<Int, Int>> by GridMovement(e)


class TestSpatialRobot {

    val mas = mas {

        environment {
            GridEnvironment()
        }



        agent("Vacuum") {

            hasInitialGoals {
                !"goal"
            }
            withSkills(CustomSkillSet()) //TODO: Cannot infer generics here
            hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        with(MyCapabilitiesSimple()) {
                            agent.print("Hello World!")
                            pippo()
                            baudo()
                            agent.terminate()
                        }
                    }

            }
        }
    }

    @Test
    fun testSkillFeature() {
        Logger.setMinSeverity(Severity.Debug)
        executeInTestScope { mas }
    }

}
