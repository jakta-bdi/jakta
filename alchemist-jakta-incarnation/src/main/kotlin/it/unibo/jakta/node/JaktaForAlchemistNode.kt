package it.unibo.jakta.node


class JaktaForAlchemistNode<Body: Any>: BaseNode<Body>() {
    val subscription: NodeSubscription = NodeNetwork.subscribe()
}
