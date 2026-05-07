package it.unibo.jakta.node

/**
 * Defines a behavior for a node, i.e. something proactively executed by it.
 */
interface NodeBehavior<Body : Any, Skills : Any> {
    /**
     * Method that starts the execution of the behavior inside of a Node.
     * @param node the [Node] instance that is executing such behavior.
     */
    suspend fun start(node: Node<Body, Skills>)
}
