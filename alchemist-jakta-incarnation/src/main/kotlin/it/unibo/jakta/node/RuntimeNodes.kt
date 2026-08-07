package it.unibo.jakta.node

/**
 * Custom Runtime for Alchemist.
 * It serves only as a JaKtA node container.
 */
class RuntimeNodes<N: JaktaForAlchemistNode<*>>(
    /**
     * The JaKtA nodes that are being executed in the node.
     */
    val nodes: Set<N>,
)
