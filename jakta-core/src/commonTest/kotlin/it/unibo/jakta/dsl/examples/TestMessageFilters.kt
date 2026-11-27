package it.unibo.jakta.dsl.examples

import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.MessageFilter
import it.unibo.jakta.skills.MessagingSkill
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMessageFilters {

    data class Position(val x: Int)

    private val alice = BaseAgentID("alice")
    private val bob = BaseAgentID("bob")
    private val carol = BaseAgentID("carol")

    private val node = node(NodeBuilders.baseNode()) {
        agent<Any, Any>(alice) { embodiedAs { Position(0) } }
        agent<Any, Any>(bob) { embodiedAs { Position(1) } }
        agent<Any, Any>(carol) { embodiedAs { Position(100) } }
    }

    // Dispatches the pending system events as a runner would, returning the added agents.
    private fun processSystemEvents(): List<ExecutableAgent<*, *>> = generateSequence { node.systemEvents.tryNext() }
        .onEach { node.handleExternalEvent(it) }
        .filterIsInstance<SystemEvent.AgentAddition<*, *>>()
        .map { it.executableAgent }
        .toList()

    private fun ExecutableAgent<*, *>.receivedPayloads() = generateSequence { events.tryNext() }
        .filterIsInstance<AgentEvent.External.Message<*>>()
        .map { it.payload }
        .toList()

    @Test
    fun `broadcast is delivered to all agents but the sender`() {
        val agents = processSystemEvents().associateBy { it.id }
        with(MessagingSkill(node)) { agents.getValue(alice).broadcast("hi") }
        processSystemEvents()
        assertEquals(emptyList(), agents.getValue(alice).receivedPayloads())
        assertEquals(listOf<Any>("hi"), agents.getValue(bob).receivedPayloads())
        assertEquals(listOf<Any>("hi"), agents.getValue(carol).receivedPayloads())
    }

    @Test
    fun `custom filter selects agents by their body`() {
        val agents = processSystemEvents().associateBy { it.id }
        val near = MessageFilter<Any> { _, _, body -> (body as Position).x < 10 }
        with(MessagingSkill(node)) { agents.getValue(alice).send("near", near) }
        processSystemEvents()
        assertEquals(listOf<Any>("near"), agents.getValue(alice).receivedPayloads())
        assertEquals(listOf<Any>("near"), agents.getValue(bob).receivedPayloads())
        assertEquals(emptyList(), agents.getValue(carol).receivedPayloads())
    }
}
