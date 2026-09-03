package it.unibo.jakta.skills

import it.unibo.jakta.node.Node

/**
 * Base abstract class for all skills, providing a reference to the [node] the skill is associated with.
 */
@Suppress("AbstractClassCanBeConcreteClass")
open class Skill<Body : Any>(protected val node: Node<Body>)
