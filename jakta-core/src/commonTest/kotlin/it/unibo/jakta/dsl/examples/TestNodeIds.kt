package it.unibo.jakta.dsl.examples

import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.NodeID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestNodeIds {

    @Test
    fun `nodes keep their explicit ids`() {
        val mas = mas(NodeBuilders.baseNode<Any>()) {
            node(NodeID("ping")) { }
            node(NodeID("pong")) { }
        }
        assertEquals(listOf("ping", "pong"), mas.nodes.map { it.id.id })
    }

    @Test
    fun `duplicated node ids are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            mas(NodeBuilders.baseNode<Any>()) {
                node(NodeID("ping")) { }
                node(NodeID("ping")) { }
            }
        }
    }
}
