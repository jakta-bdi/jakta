package it.unibo.jakta.dsl.mas

import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.node.NodeRunner
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Base implementation of a MasBuilder, with a strategy to build nodes and run them concurrently.
 * @param builderFactory the strategy factory to create new node builders, given the id of the node to build.
 */
class BaseMasBuilder<N : ExecutableNode<*>, NB : NodeBuilder<*, N>>(val builderFactory: (NodeID) -> NB) :
    MasBuilder<N, NB> {

    private val _nodes = mutableListOf<N>()

    override val nodes: List<N>
        get() = _nodes.toList()

    override fun node(block: NB.() -> Unit) {
        add(builderFactory(NodeID()).apply(block).build())
    }

    override fun node(id: NodeID, block: NB.() -> Unit) {
        val node = builderFactory(id).apply(block).build()
        require(node.id == id) {
            "The node builder factory ignored the requested id $id, " +
                "use a factory taking the NodeID, like NodeBuilders.baseNode()"
        }
        add(node)
    }

    override fun withNodes(vararg node: N) {
        node.forEach { add(it) }
    }

    private fun add(node: N) {
        require(_nodes.none { it.id == node.id }) { "The MAS already contains a node with id ${node.id}" }
        _nodes += node
    }

    override suspend fun run(runner: NodeRunner<N>) {
        supervisorScope {
            _nodes.forEach {
                launch {
                    runner.run(it)
                }
            }
        }
    }
}
