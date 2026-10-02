package it.unibo.alchemist.jakta.actions

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction

/**
 * Alchemist Action executing one [JaktaForAlchemistRuntime.step] of the JaKtA nodes hosted by an Alchemist node:
 * system events exchange and one reasoning step for each agent.
 */
class JaktaStepAction(private val runtime: JaktaForAlchemistRuntime<*>) : AbstractAction<Any?>(runtime.node) {

    // Messages may reach neighbors, but JaKtA reactions have fixed time distributions:
    // no other reaction must be rescheduled when this one executes.
    override fun getContext(): Context = Context.LOCAL

    override fun cloneAction(node: AlchemistNode<Any?>, reaction: Reaction<Any?>): Action<Any?> =
        JaktaStepAction(node.asProperty<Any?, JaktaForAlchemistRuntime<*>>())

    override fun execute() = runtime.step()
}
