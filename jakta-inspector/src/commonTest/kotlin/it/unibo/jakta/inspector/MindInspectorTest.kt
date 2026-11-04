package it.unibo.jakta.inspector

import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class MindInspectorTest {
    private val alice = BaseAgentID("alice")
    private val bob = BaseAgentID("bob")
    private val nobody = BaseAgentID("nobody")

    @Test
    fun recordsTheAgentsOfEveryNodeAndTheMessagesTheyExchange(): TestResult = runTest {
        val inspector = MindInspector()
        mas(NodeBuilders.baseNode()) {
            node {
                context(MessagingSkill(node)) {
                    agent<String, String>(alice) {
                        embodiedAs { Any() }
                        handlesMessageEvents { AgentUpdate.Belief(setOf(it.payload.toString())) }
                        hasInitialGoals { !"start" }
                        hasPlanLibrary {
                            adding.goal { takeIf { it == "start" } } triggers {
                                // an equal message to someone else, which bob's must not be mistaken for
                                agent.sendTo(nobody, "ping")
                                agent.sendTo(bob, "ping")
                            }
                            adding.belief { takeIf { it == "pong" } } triggers { node.terminateNode() }
                        }
                    }
                }
            }
            node {
                context(MessagingSkill(node)) {
                    agent<String, String>(bob) {
                        embodiedAs { Any() }
                        handlesMessageEvents { AgentUpdate.Belief(setOf(it.payload.toString())) }
                        hasPlanLibrary {
                            adding.belief { takeIf { it == "ping" } } triggers {
                                agent.sendTo(alice, "pong")
                                node.terminateNode()
                            }
                        }
                    }
                }
            }
        }.run(inspector.runner())

        val (inspectedAlice, inspectedBob) = inspector.agents.value
        assertEquals(listOf(alice, bob), listOf(inspectedAlice.id, inspectedBob.id))
        assertNotNull(inspectedAlice.node)
        assertNotEquals(inspectedAlice.node, inspectedBob.node)

        val bobHistory = inspectedBob.history.value
        assertEquals(EventKind.CREATED, bobHistory.first().kind)
        assertEquals(EventKind.MESSAGE, bobHistory[1].kind)
        val pingAdded = bobHistory.first { it.event == "+ping" }
        assertEquals(EventKind.BELIEF_ADDED, pingAdded.kind)
        assertEquals(listOf<Any>("ping"), pingAdded.beliefs)
        val alicePlans = inspectedAlice.history.value.last().plans
        assertEquals(listOf("+!goal, returning Unit", "+belief, returning Unit"), alicePlans)

        val messages = inspector.messages.value.map { Triple(it.message.payload, it.message.sender, it.receivers) }
        assertEquals(
            listOf(
                Triple("ping", alice, emptyList()),
                Triple("ping", alice, listOf(bob)),
                Triple("pong", bob, listOf(alice)),
            ),
            messages,
        )
        assertEquals("string", inspector.messages.value.first().kind)
    }

    @Test
    fun recordsTheSubGoalsOfAnIntention(): TestResult = runTest {
        val inspector = MindInspector()
        mas(NodeBuilders.baseNode()) {
            node {
                agent<String, String>(alice) {
                    embodiedAs { Any() }
                    hasInitialGoals { !"outer" }
                    hasPlanLibrary {
                        adding.goal { takeIf { it == "outer" } } triggers {
                            agent.achieve("inner")
                            node.terminateNode()
                        }
                        adding.goal { takeIf { it == "inner" } } triggers {
                            agent.believe("inside")
                            delay(10.milliseconds)
                        }
                    }
                }
            }
        }.run(inspector.runner())

        val history = inspector.agents.value.single().history.value
        val inside = history.first { it.event == "+inside" }
        assertEquals(listOf(listOf("+!outer", "+!inner")), inside.intentions.map { it.goals })
        assertTrue(history.last().intentions.none { "+!inner" in it.goals }, "the sub-goal is achieved")
    }

    @Test
    fun keepsABoundedHistory(): TestResult = runTest {
        val inspector = MindInspector(historySize = 3)
        mas(NodeBuilders.baseNode()) {
            node {
                agent<String, String>(alice) {
                    embodiedAs { Any() }
                    hasInitialGoals { !"count" }
                    hasPlanLibrary {
                        adding.goal { takeIf { it == "count" } } triggers {
                            repeat(10) { agent.believe("$it") }
                            node.terminateNode()
                        }
                    }
                }
            }
        }.run(inspector.runner())

        val history = inspector.agents.value.single().history.value
        assertEquals(3, history.size)
        assertTrue(history.none { it.kind == EventKind.CREATED })
    }
}
