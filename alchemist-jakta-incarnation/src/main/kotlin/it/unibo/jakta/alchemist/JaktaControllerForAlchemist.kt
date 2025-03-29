package it.unibo.jakta.alchemist

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.reactions.JaktaAgentForAlchemist
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.alchemist.model.Time as AlchemistTime
import it.unibo.jakta.agents.fsm.time.Time as JaktaTime

data class JaktaControllerForAlchemist(
    val jaktaEnvironment: JaktaEnvironmentForAlchemist<*>,
) : Activity.Controller {
    var minimumAwakeTime: AlchemistTime = AlchemistTime.ZERO

    override fun restart() = error("Cannot be performed during Alchemist execution")

    override fun pause() = TODO("You cannot wake up the agent once it pauses!")

    override fun resume() = TODO("Yeah good luck!")

    override fun stop() {
        jaktaEnvironment.node.reactions
            .filterIsInstance<JaktaAgentForAlchemist<*>>()
            .forEach { reaction ->
                jaktaEnvironment.node.removeReaction(reaction)
                jaktaEnvironment.alchemistEnvironment.simulation.reactionRemoved(reaction)
            }
    }

    override fun currentTime(): JaktaTime = JaktaTime.continuous(alchemistTime.toDouble())

    override fun sleep(millis: Long) {
        minimumAwakeTime = alchemistTime + DoubleTime(millis.toDouble() / 1000)
    }

    private val alchemistTime get() = jaktaEnvironment.alchemistEnvironment.simulation.time
}
