package it.unibo.jakta.node

/**
 * [Node] executed in an Alchemist simulation.
 * Its system events are exchanged by the [it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime]
 * of the Alchemist node hosting it.
 */
class JaktaForAlchemistNode<Body : Any> : BaseNode<Body>()
