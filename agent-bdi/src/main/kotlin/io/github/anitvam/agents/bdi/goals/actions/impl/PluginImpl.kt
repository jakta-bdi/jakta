package io.github.anitvam.agents.bdi.goals.actions.impl

import io.github.anitvam.agents.bdi.goals.actions.ActionID
import io.github.anitvam.agents.bdi.goals.actions.Plugin
import it.unibo.tuprolog.core.Substitution

internal data class PluginImpl(
    override val name: String,
    private val runnable: (args: List<Any>) -> Substitution,
    override val id: ActionID = ActionID(),
) : Plugin {

    override fun execute(args: List<Any>) = runnable(args)
}
