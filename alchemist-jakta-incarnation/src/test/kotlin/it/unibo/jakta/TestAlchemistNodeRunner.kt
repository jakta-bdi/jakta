package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.jakta.AlchemistNodeRunner
import it.unibo.alchemist.jakta.JaktaIncarnation
import it.unibo.alchemist.jakta.Messaging
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.situated.Coordinates
import it.unibo.jakta.situated.SituatedBody
import it.unibo.jakta.skills.AgentTerminationSkill
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.broadcast
import it.unibo.jakta.skills.sendTo
import it.unibo.jakta.skills.terminate
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class TestAlchemistNodeRunner {

    private fun simulation(): Engine<Any?, Euclidean2DPosition> {
        val environment = Continuous2DEnvironment(JaktaIncarnation<Euclidean2DPosition>())
        environment.linkingRule = ConnectWithinDistance(RANGE)
        environment.addTerminator(AfterTime(DoubleTime(MAX_TIME)))
        return Engine(environment)
    }

    private fun Engine<Any?, Euclidean2DPosition>.run(
        mas: MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>,
        messaging: Messaging = Messaging.GLOBAL,
        rate: Double = 1.0,
    ) {
        val runner = AlchemistNodeRunner<Euclidean2DPosition, BaseNode<Any>>(
            this,
            messaging,
            timeDistribution = { DiracComb(rate) },
        )
        runBlocking { mas.run(runner) }
        error.ifPresent { throw it }
    }

    // The i-th node starts at (SPACING * i, 0)
    private fun bodyOfNode(index: Int) = SituatedBody(Coordinates(SPACING * index, 0.0))

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Test
    fun testPingPongInSimulatedTime() {
        val simulation = simulation()
        val bob = BaseAgentID("Bob")
        val alice = BaseAgentID("Alice")
        var elapsed = Double.NaN
        val mas = mas(NodeBuilders.baseNode<Any>()) {
            node {
                context(MessagingSkill(node)) {
                    agent<String, String>(bob) {
                        embodiedAs { bodyOfNode(0) }
                        handlesMessageEvents { AgentUpdate.Belief(setOf(it.payload.toString()), emptySet()) }
                        hasPlanLibrary {
                            adding.belief { takeIf { it == "ping" } } triggers {
                                agent.sendTo(alice, "pong")
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
            node {
                context(MessagingSkill(node)) {
                    agent<String, String>(alice) {
                        embodiedAs { bodyOfNode(1) }
                        handlesMessageEvents { AgentUpdate.Belief(setOf(it.payload.toString()), emptySet()) }
                        hasInitialGoals { !"start" }
                        hasPlanLibrary {
                            adding.goal { takeIf { it == "start" } } triggers {
                                val start = simulation.time.toDouble()
                                delay(1500.milliseconds)
                                elapsed = simulation.time.toDouble() - start
                                agent.sendTo(bob, "ping")
                            }
                            adding.belief { takeIf { it == "pong" } } triggers {
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }
        simulation.run(mas, rate = 10.0)
        assertTrue(elapsed in 1.5..1.6, "A delay of 1.5 seconds lasted $elapsed simulated seconds")
        assertTrue(simulation.time.toDouble() < MAX_TIME, "The simulation must stop once all nodes terminated")
    }

    @Test
    fun testBodiesFollowTheAlchemistNode() {
        val simulation = simulation()
        val body = bodyOfNode(0)
        var seen: Coordinates? = null
        val mas = mas(NodeBuilders.baseNode<Any>()) {
            node {
                agent<String, String>(BaseAgentID("moved")) {
                    embodiedAs { body }
                    hasInitialGoals { !"wait" }
                    hasPlanLibrary {
                        adding.goal { takeIf { it == "wait" } } triggers {
                            // moved by Alchemist, not by the agent
                            with(simulation.environment) { moveNodeToPosition(nodes.single(), makePosition(3.0, 4.0)) }
                            delay(1.seconds)
                            seen = body.position
                            node.terminateNode()
                        }
                    }
                }
            }
        }
        simulation.run(mas)
        assertEquals(Coordinates(3.0, 4.0), seen)
    }

    // Three nodes on a line, only consecutive ones are neighbors: A - B - C
    private fun receiversOfBroadcast(messaging: Messaging): Set<String> {
        val receivers = ConcurrentHashMap.newKeySet<String>()
        val mas = mas(NodeBuilders.baseNode<Any>()) {
            listOf("A", "B", "C").forEachIndexed { index, name ->
                node {
                    context(MessagingSkill(node), AgentTerminationSkill(node)) {
                        agent<String, String>(BaseAgentID(name)) {
                            embodiedAs { bodyOfNode(index) }
                            handlesMessageEvents {
                                receivers += name
                                null
                            }
                            if (name == "A") {
                                hasInitialGoals { !"broadcast" }
                            }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "broadcast" } } triggers {
                                    agent.broadcast("hello")
                                    agent.terminate()
                                }
                            }
                        }
                    }
                }
            }
        }
        simulation().run(mas, messaging)
        return receivers
    }

    @Test
    fun testGlobalMessaging() {
        assertEquals(setOf("B", "C"), receiversOfBroadcast(Messaging.GLOBAL))
    }

    @Test
    fun testNeighborhoodMessaging() {
        assertEquals(setOf("B"), receiversOfBroadcast(Messaging.NEIGHBORHOOD))
    }

    private companion object {
        const val RANGE = 5.0
        const val SPACING = 4.0
        const val MAX_TIME = 20.0
    }
}
