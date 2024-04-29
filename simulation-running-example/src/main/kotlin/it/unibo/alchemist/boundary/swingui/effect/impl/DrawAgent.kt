package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.molecules.SimpleMolecule
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Ellipse2D

@Suppress("DEPRECATION")
class DrawAgent : it.unibo.alchemist.boundary.swingui.effect.api.Effect {

    override fun getColorSummary(): Color = Color.MAGENTA

    override fun <T, P : Position2D<P>?> apply(
        graphics: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        val zoom: Double = wormhole.zoom
        val viewPoint: Point = wormhole.getViewPoint(environment.getPosition(node))
        draw(graphics, node, zoom, viewPoint)
    }

    private fun <T> draw(
        graphics: Graphics2D,
        node: Node<T>,
        zoom: Double,
        point: Point,
    ) {
        if (node.contents.keys.contains(SimpleMolecule("sightRadius"))) {
            val sightRadius: Double = node.contents[SimpleMolecule("sightRadius")] as Double
            graphics.color = colorSummary
            val size = sightRadius * zoom
            graphics.draw(
                Ellipse2D.Double(
                    point.x - size / 2,
                    point.y - size / 2,
                    size,
                    size,
                ),
            )
        }
    }
}
