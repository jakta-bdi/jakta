/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.jakta.timedistributions

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.jakta.alchemist.JaktaControllerForAlchemist
import it.unibo.jakta.alchemist.JaktaLifecyclePhase.ACT
import it.unibo.jakta.alchemist.JaktaLifecyclePhase.DELIBERATE
import it.unibo.jakta.alchemist.JaktaLifecyclePhase.SENSE

class JaktaTimeDistribution(
    node: JaktaEnvironmentForAlchemist<*>,
    val sense: TimeDistribution<Any?>,
    val deliberate: TimeDistribution<Any?>,
    val act: TimeDistribution<Any?>,
) : TimeDistribution<Any?> {
    var phase = SENSE
        private set

    val controller: JaktaControllerForAlchemist = JaktaControllerForAlchemist(node)

    val currentBackingTimeDistribution get() =
        when (phase) {
            SENSE -> sense
            DELIBERATE -> deliberate
            ACT -> act
        }

    override fun update(
        currentTime: Time,
        executed: Boolean,
        param: Double,
        environment: Environment<Any?, *>,
    ) {
        if (executed) {
            phaseComplete()
            currentBackingTimeDistribution.update(
                maxOf(currentTime, controller.minimumAwakeTime),
                true,
                param,
                environment,
            )
        }
    }

    override fun getNextOccurence(): Time =
        currentBackingTimeDistribution
            .nextOccurence
            .takeIf { it > controller.minimumAwakeTime }
            ?: controller.minimumAwakeTime

    override fun getRate(): Double = currentBackingTimeDistribution.rate

    private fun phaseComplete() {
        phase = phase.next()
    }

    override fun cloneOnNewNode(
        destination: Node<Any?>,
        currentTime: Time,
    ): TimeDistribution<Any?> =
        JaktaTimeDistribution(
            destination.asProperty(),
            sense.cloneOnNewNode(destination, currentTime),
            deliberate.cloneOnNewNode(destination, currentTime),
            act.cloneOnNewNode(destination, currentTime),
        )

    override fun toString(): String = "Jakta(next phase: $phase, scheduler: $currentBackingTimeDistribution)"
}
