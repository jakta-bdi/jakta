package it.unibo.jakta.mas

import it.unibo.jakta.intention.IntentionDispatcher
import it.unibo.jakta.node.LocalNodeBuilder
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeBuilder
import it.unibo.jakta.node.NodeRunner
import it.unibo.jakta.node.baseImpl.CoroutineNodeRunner
import it.unibo.jakta.plan.triggers
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface MasBuilder<N: Node<*, *>, NB: NodeBuilder<*, *, *, *, N>> {

    fun node(block: NB.() -> Unit)

    suspend fun run(runner: NodeRunner<N>)
}

fun <N: Node<*, *>, NB: NodeBuilder<*, *, *, *, N>> mas (builder: NB, block: MasBuilder<N, NB>.() -> Unit)
    : MasBuilder<N, NB> {
    return object : MasBuilder<N, NB> {

        val nodes = mutableListOf<N>()

        override fun node(block: NB.() -> Unit) {
            nodes += builder.apply(block).build()
        }

        override suspend fun run(runner: NodeRunner<N>) {
            nodes.forEach { runner.run(it) }
        }
    }.apply(block)
}


//TODO this is a syntax test but it does not run due to
// Kotlin multiplatform limitations on suspend main

suspend fun main(): Unit = coroutineScope {

    mas(LocalNodeBuilder()) {
        node {
            agent("Hello world agent") {
                body = object {}
                withSkills { object {} }
                believes {
                    +"testBelief"
                }
                hasPlans {
                    adding.belief {
                        this.takeIf { it == "testBelief" }
                    } triggers {
                        agent.print("Belief added: $context")
                    }
                }
            }
        }

    }.run(CoroutineNodeRunner())
}
