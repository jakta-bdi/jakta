package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.node.Node

fun <Skills : Any, Body : Any> node(
    block: LocalNodeBuilder<String, String, Skills, Body>.() -> Unit,
): Node<Body, Skills> {
    val nodeBuilder = LocalNodeBuilder<String, String, Skills, Body>()
    nodeBuilder.apply(block)
    return nodeBuilder.build()
}
