@file:JvmName("AlchemistGossip")

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.skills.MessagingSkill

/**
 * Entrypoint of every Alchemist node in `gossip.yml`: one gossiper, with the situated skills of its Alchemist node.
 * Its position and the `source` property come from the simulation file.
 */
fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(NodeBuilders.baseNode<Any>()) {
    Logger.setMinSeverity(Severity.Warn)
    node {
        context(skills, MessagingSkill(node)) {
            gossiper(BaseAgentID("agent-${alchemistNode.id}")) { Any() }
        }
    }
}
