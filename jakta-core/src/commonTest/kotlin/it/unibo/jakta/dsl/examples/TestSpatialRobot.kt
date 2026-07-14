package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.examples.Movement.Events.Factory.position
import it.unibo.jakta.dsl.examples.Recharging.Events.Factory.chargeLevel
import it.unibo.jakta.dsl.executeInTestScope
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

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
    context(s1: Recharging)
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
        node.publishEvent(chargeLevel(100), { it == node.agents[agent.id] })
    }
}

class GridMovement(val node: Node<BodyWithPosition>) : Movement<DoubleArray> {
    context(s1: Recharging)
    override fun Agent.moveTo(newPos: DoubleArray) {
        node.agents[this.id]?.position2D = newPos
        node.publishEvent(position(this.id, newPos))
    }
}

class TestSpatialRobot {

    val mas = node(NodeBuilders.baseNode<BodyWithPosition>()) {
        context(
            FixedTimeRecharging(node),
            GridMovement(node),
        ) {
            agent(BaseAgentID("robot")) {
                embodiedAs { BodyWithPosition() }

                believes {
                    +"temp(0)"
                }

                hasInitialGoals {
                    !"goal"
                }

                handlesPerceptionEvents { event ->
                    when (event) {
                        is Movement.Events.Position<*> ->
                            AgentUpdate.Belief(
                                setOf(("position(${event.agentId}, ${event.position})")),
                                beliefs.filter { it.startsWith("position(") }.toSet(),
                            )

                        is Recharging.Events.ChargeLevel ->
                            AgentUpdate.Belief(
                                setOf("chargeLevel(${event.level})"),
                                beliefs.filter { it.startsWith("chargeLevel(") }.toSet(),
                            )

                        else -> null
                    }
                }

                hasPlans {
                    adding.goal {
                        ifGoalMatch("goal")
                    } triggers {
                        with(contextOf<Recharging>()) {
                            agent.print("Hello World!")
                            agent.recharge()
                            agent.print("Recharged!")
                        }
                        with(contextOf<Movement<DoubleArray>>()) {
                            agent.moveTo(doubleArrayOf(0.0, 1.0))
                            agent.print("Moved!")
                        }
                        node.terminateNode()
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
