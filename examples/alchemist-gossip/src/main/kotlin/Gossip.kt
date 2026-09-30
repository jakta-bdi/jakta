@file:JvmName("Gossip")

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.alchemistNode
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.broadcast

/**
 * The rumor spread by the agents.
 */
const val RUMOR = "rumor"

/**
 * Molecule marking the Alchemist node whose agent knows the rumor first, set in the simulation file.
 */
val SOURCE = SimpleMolecule("source")

/**
 * Molecule set on the Alchemist node of every agent that knows the rumor, for the exporters and the GUI.
 */
val INFORMED = SimpleMolecule("informed")

/**
 * Entrypoint of every Alchemist node: one agent that, when it learns the rumor, broadcasts it once.
 * With `messaging: neighborhood` the broadcast only reaches the neighbors, so the rumor floods the network hop by hop.
 */
fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(NodeBuilders.alchemistNode<Any>()) {
    Logger.setMinSeverity(Severity.Warn)
    node {
        context(MessagingSkill(node)) {
            agent<String, String>(BaseAgentID("agent-${alchemistNode.id}")) {
                embodiedAs { Any() }
                handlesMessageEvents { message ->
                    (message.payload as? String)?.let { AgentUpdate.Belief(setOf(it), emptySet()) }
                }
                if (alchemistNode.contains(SOURCE)) {
                    hasInitialGoals { !"start" }
                }
                hasPlanLibrary {
                    adding.goal { takeIf { it == "start" } } triggers {
                        agent.believe(RUMOR)
                    }
                    // Beliefs are a set: hearing the rumor again adds no belief, so each agent broadcasts it once
                    adding.belief { takeIf { it == RUMOR } } triggers {
                        alchemistNode.setConcentration(INFORMED, true)
                        agent.print("Informed at time ${alchemistEnvironment.simulation.time}")
                        agent.broadcast(RUMOR)
                    }
                }
            }
        }
    }
}
