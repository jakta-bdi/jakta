package it.unibo.alchemist.jakta.actions

import co.touchlab.kermit.Logger
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

class JaktaForAlchemistAgent<P: Position<P>>(
    alchemistNode: AlchemistNode<Any?>,
    private val agent: ExecutableAgent<*, *, *>,
): AbstractAction<Any?>(alchemistNode) {

    override fun getContext(): Context = Context.LOCAL
    private val scope = CoroutineScope(Dispatchers.Main)
    private var agentLifecycle = BaseAgentLifecycle(agent)

    override fun cloneAction(
        node: AlchemistNode<Any?>,
        reaction: Reaction<Any?>,
    ): Action<Any?> = JaktaForAlchemistAgent(
        node,
        agent,
    )

    override fun execute() {
        val logger = Logger(
            Logger.config,
            "Agent Action",
        )

        logger.d {"Before"}

                logger.d { "Entered runBlocking" }
                    agentLifecycle.tryStep(scope)
                    logger.d { "During" }


        logger.d {"After"}

    }
}
