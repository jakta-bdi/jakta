package examples

import TerminationSkill
import TerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import examples.Movement.Events.Factory.position
import examples.Recharging.Events.Factory.chargeLevel
import examples.TemperatureSensing.Events.Factory.temperature
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.jakta
import it.unibo.jakta.node.AgentBody
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlinx.coroutines.delay

class BodyWithPosition : AgentBody {
    val position2D: DoubleArray = doubleArrayOf(0.0, 0.0)
}

// TODO if we have this, bound the generics everywhere??
interface Skill {
    suspend fun start(node: Node<BodyWithPosition, *>) {
        // default no-op implementation
    }
}

interface Recharging : Skill {
    suspend fun recharge()

    object Events {
        data class ChargeLevel internal constructor(val level: Int) : AgentEvent.External.Perception

        // factory for events of Recharging
        // using the receiver prevents usage from outside extensions of the Recharging interface
        object Factory {
            fun Recharging.chargeLevel(level: Int): ChargeLevel = ChargeLevel(level)
        }
    }
}

interface Movement<P> : Skill {
    // context(s1: Recharging) TODO this does not work right yet due to "triggers" not having context propagation
    fun moveTo(newPos: P)

    object Events {
        data class Position<P> internal constructor(val position: P) : AgentEvent.External.Perception

        object Factory {
            fun <P> Movement<P>.position(pos: P): Position<P> = Position(pos)
        }
    }
}

class FixedTimeRecharging(val node: Node<BodyWithPosition, *>) : Recharging {
    override suspend fun recharge() {
        delay(3000)
        node.agents.forEach { it.externalInbox.send(chargeLevel(100)) } // TODO: wrong
    }
}

class GridMovement(val node: Node<BodyWithPosition, *>) : Movement<Pair<Int, Int>> {
    // context(s1: Recharging)
    override fun moveTo(newPos: Pair<Int, Int>) {
        node.agents.forEach {
            it.externalInbox.send(position(newPos))
        } // TODO: Not correct, all agents try to go to the same position
    }
}

// Skill with active behavior that "runs" in the environment
interface TemperatureSensing : Skill {
    // TODO we need a way to have skills that have an active behavior
    // who calls this method??
    override suspend fun start(node: Node<BodyWithPosition, *>) {
        startSensing(node)
    }

    suspend fun startSensing(node: Node<BodyWithPosition, *>)

    object Events {
        data class Temperature internal constructor(val value: Float) : AgentEvent.External.Perception

        object Factory {
            fun TemperatureSensing.temperature(value: Float): Temperature = Temperature(value)
        }
    }
}

// This skill is a singleton, shared by all agents using it
object FixedIntervalTemperatureSensing : TemperatureSensing {

    override suspend fun startSensing(node: Node<BodyWithPosition, *>) {
        while (true) {
            delay(5000)
            val temp = (15..30).random() + (0..99).random() / 100f
            Logger.i { "Sensed temperature: $temp °C" }
            // here we would send the event to the environment or agent
            node.agents.forEach {
                it.externalInbox.send(this.temperature(temp))
            } // TODO: Not correct, all agents see the same position
        }
    }
}

class CustomSkillSet(val node: Node<BodyWithPosition, *>) :
    Recharging by FixedTimeRecharging(node),
    Movement<Pair<Int, Int>> by GridMovement(node),
    TemperatureSensing by FixedIntervalTemperatureSensing,
    TerminationSkill by TerminationSkillImpl(node) {
    // TODO this seems doable, but you need to remember to actually start everything
    // And you should know what needs to start... not great
    // and then who calls this??
    override suspend fun start(node: Node<BodyWithPosition, *>) {
        FixedIntervalTemperatureSensing.start(node)
    }
}

class TestSpatialRobot {

    val mas = jakta {
        agent("Vacuum") {
            body = BodyWithPosition()
            withSkills {
                CustomSkillSet(this@jakta.node)
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
                    skills.terminate()
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
