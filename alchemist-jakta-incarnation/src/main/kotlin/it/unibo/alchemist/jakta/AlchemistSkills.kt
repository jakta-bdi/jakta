package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.situated.Coordinates
import it.unibo.jakta.situated.NeighborhoodSkill
import it.unibo.jakta.situated.PropertySkill
import it.unibo.jakta.situated.Situated
import it.unibo.jakta.situated.SpatialSkill

/**
 * The situated skills of the agents hosted by an Alchemist node.
 * The agents of the same Alchemist node share its position and its molecules: a property is the molecule with its name.
 * The position of the Alchemist node is copied into the [Situated] bodies of its agents
 * at each step of the node and whenever they move.
 * The neighbors of an agent are the other agents of the same Alchemist node and of its neighbors, per the linking rule.
 * @param runtime gives the runtime of the Alchemist node, when a skill is used.
 */
class AlchemistSkills<P : Position<P>>(private val runtime: () -> JaktaForAlchemistRuntime<P>) :
    SpatialSkill,
    NeighborhoodSkill,
    PropertySkill {

    override val Agent.position: Coordinates
        get() = runtime().position

    override fun Agent.moveTo(position: Coordinates) = runtime().moveTo(position)

    override val Agent.neighbors: Set<AgentID>
        get() = runtime().reachableNodes().flatMap { it.hostedAgents }.toSet() - id

    override fun Agent.property(name: String): Any? = with(runtime().alchemistNode) {
        SimpleMolecule(name).let { if (contains(it)) getConcentration(it) else null }
    }

    override fun Agent.setProperty(name: String, value: Any?) = runtime().setProperty(name, value)
}
