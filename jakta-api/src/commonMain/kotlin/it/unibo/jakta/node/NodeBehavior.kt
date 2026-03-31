package it.unibo.jakta.node

interface NodeBehavior<Body: Any, Skills: Any> {
    suspend fun start(node: Node<Body, Skills>)
}
