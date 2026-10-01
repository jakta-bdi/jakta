package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.situated.Coordinates
import it.unibo.jakta.situated.NeighborhoodSkill
import it.unibo.jakta.situated.PropertySkill
import it.unibo.jakta.situated.SpatialSkill

/**
 * The situated skills of the agents hosted by an Alchemist node.
 * The agents of the same Alchemist node share its position and its molecules: a property is the molecule with its name.
 * The neighbors of an agent are the other agents of the same Alchemist node and of its neighbors, per the linking rule.
 * @param runtime gives the runtime of the Alchemist node, when a skill is used.
 */
class AlchemistSkills<P : Position<P>>(private val runtime: () -> JaktaForAlchemistRuntime<P>) :
    SpatialSkill,
    NeighborhoodSkill,
    PropertySkill {

    override val Agent.position: Coordinates
        get() = with(runtime()) { Coordinates(alchemistEnvironment.getPosition(alchemistNode).coordinates.toList()) }

    override fun Agent.moveTo(position: Coordinates) = with(runtime()) {
        alchemistEnvironment.moveNodeToPosition(alchemistNode, alchemistEnvironment.makePosition(position.values))
    }

    override val Agent.neighbors: Set<AgentID>
        get() = runtime().reachableNodes().flatMap { it.hostedAgents }.toSet() - id

    override fun Agent.property(name: String): Any? = with(runtime().alchemistNode) {
        SimpleMolecule(name).let { if (contains(it)) getConcentration(it) else null }
    }

    override fun Agent.setProperty(name: String, value: Any?) = runtime().setProperty(name, value)
}
