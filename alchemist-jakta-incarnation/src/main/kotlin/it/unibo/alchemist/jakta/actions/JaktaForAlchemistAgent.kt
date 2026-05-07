package it.unibo.alchemist.jakta.actions

import co.touchlab.kermit.Logger
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.alchemist.AlchemistDispatcher

/**
 * Alchemist Action executing the JaKtA agent lifecycle.
 */
class JaktaForAlchemistAgent<P : Position<P>>(
    alchemistNode: AlchemistNode<Any?>,
    private val agent: ExecutableAgent<*, *, *>,
    private val alchemistEnvironment: Environment<Any?, P>,
) : AbstractAction<Any?>(alchemistNode) {

    override fun getContext(): Context = Context.LOCAL

    private val agentLifecycle = BaseAgentLifecycle(agent)
    private val dispatcher = AlchemistDispatcher.of(alchemistEnvironment)
    private val logger = Logger(Logger.config, "Agent Action")

    override fun cloneAction(node: AlchemistNode<Any?>, reaction: Reaction<Any?>): Action<Any?> =
        JaktaForAlchemistAgent(
            node,
            agent,
            alchemistEnvironment,
        )

    override fun execute() {
        logger.d { "Executing Agent Step" }
        // dispatcher.runDueTasks()
        agentLifecycle.tryStep(dispatcher)
        dispatcher.runDueTasks() // TODO: This is not the best position to invoke this
    }
}
