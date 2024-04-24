package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import kotlin.math.roundToInt

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
        val x: Int = viewPoint.x
        val y: Int = viewPoint.y
        if (environment is Physics2DEnvironment) {
            draw(graphics, node, environment, zoom, x, y)
        }
    }

    private fun <T> draw(
        graphics: Graphics2D,
        node: Node<T>,
        environment: Physics2DEnvironment<T>,
        zoom: Double,
        x: Int,
        y: Int,
    ) {
        if (node.contents.keys.contains(SimpleMolecule("sightRadius"))) {
            val sightRadius: Double = node.contents[SimpleMolecule("sightRadius")] as Double
            graphics.color = colorSummary
            println(environment + zoom)
            graphics.drawOval(
                x - sightRadius.roundToInt(),
                y - sightRadius.roundToInt(),
                (sightRadius * 2).roundToInt(),
                (sightRadius * 2).roundToInt(),
            )
        }
    }
}
