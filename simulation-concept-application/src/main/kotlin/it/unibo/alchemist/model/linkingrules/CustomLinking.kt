package it.unibo.alchemist.model.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.neighborhoods.Neighborhoods

class CustomLinking<T, P : Position<P>> : LinkingRule<T, P> {

    override fun isLocallyConsistent(): Boolean = false

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>): Neighborhood<T> {
        if (center.contains(SimpleMolecule("sightRadius"))) {
            val radius = center.getConcentration(SimpleMolecule("sightRadius")) as Double
            return Neighborhoods.make(environment, center, environment.getNodesWithinRange(center, radius))
        }
        return Neighborhoods.make(environment, center)
    }
}
