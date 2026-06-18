package examples

import NodeTerminationSkill
import NodeTerminationSkillImpl
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import examples.Movement.Events.Factory.position
import examples.Recharging.Events.Factory.chargeLevel
import executeInTestScope
import ifGoalMatch
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.node
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.PlanScope
import it.unibo.jakta.plan.triggers
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import terminateNode

class BodyWithPosition {
    var position2D: DoubleArray = doubleArrayOf(0.0, 0.0)
}

interface Recharging {
    suspend fun Agent.recharge()

    object Events {
        class ChargeLevel internal constructor(val level: Int) : AgentEvent.External.Perception

        // factory for events of Recharging
        // using the receiver prevents usage from outside extensions of the Recharging interface
        object Factory {
            fun Recharging.chargeLevel(level: Int): ChargeLevel = ChargeLevel(level)
        }
    }
}

interface Movement<P> {
    // context(s1: Recharging) TODO this does not work right yet due to "triggers" not having context propagation
    fun Agent.moveTo(newPos: P)

    object Events {
        class Position<P> internal constructor(val agentId: AgentID, val position: P) : AgentEvent.External.Perception

        object Factory {
            fun <P> Movement<P>.position(agentID: AgentID, pos: P): Position<P> = Position(agentID, pos)
        }
    }
}

class FixedTimeRecharging(val node: Node<BodyWithPosition>) : Recharging {
    override suspend fun Agent.recharge() {
        val agent = this
        delay(3.seconds)
        node.sendEvent(chargeLevel(100), { it == node.agents[agent.id] })
    }
}

class GridMovement(val node: Node<BodyWithPosition>) : Movement<DoubleArray> {
    override fun Agent.moveTo(newPos: DoubleArray) {
        node.agents[this.id]?.position2D = newPos
        node.sendEvent(position(this.id, newPos))
    }
}

//class CustomSkillSet(val node: Node<BodyWithPosition>) :
//    Recharging by FixedTimeRecharging(node),
//    Movement<DoubleArray> by GridMovement(node),
//    NodeTerminationSkill by NodeTerminationSkillImpl(node)

context(skill: Recharging)
val PlanScope<*, *, *>.rechargeSkill: Recharging
    get() = skill

context(skill: Movement<P>)
fun <P> PlanScope<*, *, *>.movementSkill() : Movement<P> = skill

class TestSpatialRobot {

    val mas = node {
        context(
            FixedTimeRecharging(node),
            GridMovement(node),
            NodeTerminationSkillImpl(node)
        ) {
            agent("Vacuum") {
                embodiedAs { BodyWithPosition() }

                handlesPerceptionEvents {
                    when (it) {
                        is Movement.Events.Position<*> ->
                            BeliefAddEvent("position(${it.agentId}, ${it.position})")

                        is Recharging.Events.ChargeLevel ->
                            BeliefAddEvent("chargeLevel(${it.level})")

                        else -> null
                    }
                }

                believes {
                    +"temp(0)"
                }

                hasInitialGoals {
                    !"goal"
                }
                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        with(rechargeSkill) {
                            agent.print("Hello World!")
                            agent.recharge()
                            agent.print("Recharged!")
                        }
                        with(movementSkill()) {
                            agent.moveTo(doubleArrayOf(0.0, 1.0))
                            agent.print("Moved!")
                        }
                        terminateNode()
                    }

                    adding.belief { this } triggers {
                        agent.print("Now I believe ${this.context}")
                    }
                }
            }
        }
    }

    @Test
    fun testSkillFeature() {
        Logger.setMinSeverity(Severity.Error)
        executeInTestScope { mas }
    }
}
