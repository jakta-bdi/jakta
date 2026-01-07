package examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import examples.Movement.Events.Factory.position
import examples.Recharging.Events.Factory.chargeLevel
import examples.TemperatureSensing.Events.Factory.temperature
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.environment.BaseEnvironment
import it.unibo.jakta.environment.DefaultSkills
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.event.Event
import it.unibo.jakta.mas
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay


open class SpatialEnvironment<P> : BaseEnvironment() {
    val positions: Map<AgentID, P> = emptyMap()
}


//TODO if we have this, bound the generics everywhere??
interface Skill {
    suspend fun start(e: Environment) {
        // default no-op implementation
    }
}


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
    //context(s1: Recharging) TODO this does not work right yet due to "triggers" not having context propagation
    fun moveTo(newPos: P)

    object Events {
        data class Position<P> internal constructor(val position: P) : Event.External.Perception

        object Factory {
            fun <P> Movement<P>.position(pos: P): Position<P> = Position(pos)
        }
    }
}

class FixedTimeRecharging(val e: SendChannel<Event.External.Perception>) : Recharging {
    override suspend fun recharge() {
        delay(3000)
        e.send(chargeLevel(100))
    }
}

class GridMovement(val e: GridEnvironment) : Movement<Pair<Int, Int>> {
    //context(s1: Recharging)
    override fun moveTo(newPos: Pair<Int, Int>) {
        e.trySend(position(newPos))
    }
}


// Skill with active behavior that "runs" in the environment
interface TemperatureSensing : Skill {
    //TODO we need a way to have skills that have an active behavior
    // who calls this method??
    override suspend fun start(e: Environment) {
        startSensing(e)
    }

    suspend fun startSensing(e: Environment)

    object Events {
        data class Temperature internal constructor(val value: Float) : Event.External.Perception

        object Factory {
            fun TemperatureSensing.temperature(value: Float): Temperature = Temperature(value)
        }
    }
}


// This skill is a singleton, shared by all agents using it
object FixedIntervalTemperatureSensing : TemperatureSensing {

    override suspend fun startSensing(e: Environment) {
        while (true) {
            delay(5000)
            val temp = (15..30).random() + (0..99).random() / 100f
            Logger.i { "Sensed temperature: $temp °C" }
            // here we would send the event to the environment or agent
            e.send(this.temperature(temp))
        }
    }
}


class GridEnvironment : SpatialEnvironment<Pair<Int, Int>>()

class CustomSkillSet(val env: GridEnvironment) :
    Recharging by FixedTimeRecharging(env),
    Movement<Pair<Int, Int>> by GridMovement(env),
    TemperatureSensing by FixedIntervalTemperatureSensing
{
    //TODO this seems doable, but you need to remember to actually start everything
    // And you should know what needs to start... not great
    // and then who calls this??
    override suspend fun start(e: Environment) {
        FixedIntervalTemperatureSensing.start(e)
    }
}

//TODO Attenzione
// ogni agente ha le sue skill, non condivise con altri agenti
// le skill possono mantenere stato interno
// lo stato condiviso tra agenti deve essere mantenuto nell'environment
// oppure con skill che sono dei singleton ??

class TestSpatialRobot {

    val mas = mas {

        environment {
            GridEnvironment()
        }

        agent("Vacuum") {

            withSkills {
                CustomSkillSet(it)
            }

            hasInitialGoals {
                !"goal"
            }
            hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        agent.print("Hello World!")
                        skills.recharge()
                        skills.moveTo(1 to 1)
                        agent.terminate()
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
