@file:JvmName("AgentWithTimeout")

package it.unibo.jakta.test

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.alchemistNode
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withTimeoutOrNull

/** The simulated time at which the agent started waiting. */
var waitStartedAt: Double? = null

/** The simulated time at which the agent gave up waiting. */
var waitTimedOutAt: Double? = null

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypointWithTimeout() = device(NodeBuilders.alchemistNode()) {
    node {
        agent<String, String>(BaseAgentID("Alice")) {
            embodiedAs { Any() }
            hasInitialGoals { !"wait" }
            hasPlanLibrary {
                adding.goal {
                    ifGoalMatch("wait")
                } triggers {
                    waitStartedAt = alchemistEnvironment.simulation.time.toDouble()
                    // a timeout, not a delay: it goes through the dispatcher's invokeOnTimeout
                    withTimeoutOrNull(5.seconds) { awaitCancellation() }
                    waitTimedOutAt = alchemistEnvironment.simulation.time.toDouble()
                    node.terminateNode()
                }
            }
        }
    }
}
