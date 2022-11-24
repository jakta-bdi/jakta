package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.actions.impl.PluginImpl
import it.unibo.tuprolog.core.Substitution

interface Plugin : Action {

    companion object {
        fun of(name: String, action: (args: List<Any>) -> Substitution): Plugin = PluginImpl(name, action)
    }
}
