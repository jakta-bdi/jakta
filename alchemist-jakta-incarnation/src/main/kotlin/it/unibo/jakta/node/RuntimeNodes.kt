package it.unibo.jakta.node

/**
 * Custom Runtime for Alchemist.
 * It serves only as a JaKtA node container.
 */
class RuntimeNodes<N : ExecutableNode<*>>(
    /**
     * The JaKtA nodes that are being executed in the node.
     */
    val nodes: Set<N>,
)
