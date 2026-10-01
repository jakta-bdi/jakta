import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.node.NodeBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.situated.NeighborhoodSkill
import it.unibo.jakta.situated.PropertySkill
import it.unibo.jakta.situated.neighbors
import it.unibo.jakta.situated.property
import it.unibo.jakta.situated.setProperty
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo

/**
 * The rumor spread by the agents.
 */
const val RUMOR = "rumor"

/**
 * Property of the agent that knows the rumor first.
 */
const val SOURCE = "source"

/**
 * Property of the agents that know the rumor.
 */
const val INFORMED = "informed"

/**
 * An agent that, when it learns the rumor, marks itself as informed, tells it to its neighbors, and stops its node.
 * It only relies on generic skills, so it runs in memory as well as in Alchemist.
 */
context(_: NeighborhoodSkill, _: PropertySkill, _: MessagingSkill)
fun NodeBuilder<Any, *>.gossiper(id: AgentID, body: () -> Any) = agent<String, String>(id) {
    embodiedAs { body() }
    handlesMessageEvents { message ->
        (message.payload as? String)?.let { AgentUpdate.Belief(setOf(it), emptySet()) }
    }
    hasInitialGoals { !"start" }
    hasPlanLibrary {
        adding.goal { takeIf { it == "start" } } triggers {
            if (agent.property(SOURCE) == true) {
                agent.believe(RUMOR)
            }
        }
        // Beliefs are a set: hearing the rumor again adds no belief, so each agent tells it once
        adding.belief { takeIf { it == RUMOR } } triggers {
            agent.setProperty(INFORMED, true)
            agent.print("Informed")
            agent.neighbors.forEach { agent.sendTo(it, RUMOR) }
            node.terminateNode()
        }
    }
}
