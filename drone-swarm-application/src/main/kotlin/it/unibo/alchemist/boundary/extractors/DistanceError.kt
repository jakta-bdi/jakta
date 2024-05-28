package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.examples.swarm.CircleMovement
import it.unibo.jakta.examples.swarm.SwarmPosition
import kotlin.math.PI
import kotlin.math.hypot

class DistanceError<P : Position<P>>(
    override val columnNames: List<String> = listOf("error"),
) : AbstractDoubleExporter() {
    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> {
        val (leaders, followers) = environment.nodes.partition { it.getConcentration(leader) == true }
        check(leaders.size == 1)
        val leader = leaders.first()
        val radius = checkNotNull((leader.getConcentration(radius) as? Number)).toDouble()
        val leaderPosition = environment.getPosition(leader)
        val angle = (2 * PI) / followers.count()
        val error = followers.sorted().mapIndexed { index, follower ->
            val idealAngle = index * angle
            val idealPosition = CircleMovement.positionInCircumference(
                radius,
                idealAngle,
                SwarmPosition.fromPosition(leaderPosition),
            )
            val diff = idealPosition - SwarmPosition.fromPosition(environment.getPosition(follower))
            hypot(diff.x, diff.y)
        }.sum()
        return mapOf(columnNames.first() to error)
    }

    companion object {
        val leader = SimpleMolecule("leader")
        val radius = SimpleMolecule("radius")
    }
}
