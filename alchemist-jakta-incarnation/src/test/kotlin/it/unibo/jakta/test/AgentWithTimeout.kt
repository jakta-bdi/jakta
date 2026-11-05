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
                    // no event matches: the wait ends with its timeout
                    agent.wait<Unit>({ null }, 5.seconds)
                    waitTimedOutAt = alchemistEnvironment.simulation.time.toDouble()
                    node.terminateNode()
                }
            }
        }
    }
}
