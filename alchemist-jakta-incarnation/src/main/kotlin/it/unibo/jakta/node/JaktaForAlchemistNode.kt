package it.unibo.jakta.node

/**
 * [Node] specialization of [BaseNode] which holds the [NodeSubscription],
 * made for the execution in simulation.
 */
class JaktaForAlchemistNode<Body : Any> : BaseNode<Body>() {
    /**
     * The [NodeSubscription] for communicating with the [NodeNetwork].
     */
    val subscription: NodeSubscription = NodeNetwork.trySubscribe()
        ?: error("It was not possible to subscribe this Node to the NodeNetwork.")
}
