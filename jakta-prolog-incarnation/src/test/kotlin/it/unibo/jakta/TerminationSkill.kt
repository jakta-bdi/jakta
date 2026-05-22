package it.unibo.jakta

import it.unibo.jakta.node.Node

class TerminationSkill(val node: Node<*, *>) {
    fun terminateNode() {
        node.terminateNode()
    }
}
