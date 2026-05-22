package it.unibo.jakta.dsl

import it.unibo.jakta.node.Node

class TerminationSkill(val node: Node<*, *>) {
    fun terminateNode() {
        node.terminateNode()
    }
}
