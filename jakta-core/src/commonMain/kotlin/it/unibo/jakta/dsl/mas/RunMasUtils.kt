package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork

/**
 * Utility function to run a mas with a [CoroutineNodeRunner] and a [SharedMemoryNetwork].
 */
suspend fun <B : Any> MasBuilder<BaseNode<B>, BaseNodeBuilder<B, BaseNode<B>>>.runLocally() {
    this.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
