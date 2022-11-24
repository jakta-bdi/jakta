package io.github.anitvam.agents.bdi.goals.actions.impl

import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.ActionGoal
import io.github.anitvam.agents.bdi.goals.actions.Action
import io.github.anitvam.agents.bdi.goals.actions.ActionLibrary
import io.github.anitvam.agents.bdi.goals.actions.Artifact
import io.github.anitvam.agents.bdi.goals.actions.Plugin
import it.unibo.tuprolog.core.Substitution

internal class ActionLibraryImpl : ActionLibrary {
    override val actions: List<Action>
        get() = listOf(
            Plugin.of("print") {
                println(it.firstOrNull())
                Substitution.empty()
            }
        )

    override fun lookup(actionGoal: ActionGoal) = when (actionGoal) {
        is ActInternally -> actions.filterIsInstance<Plugin>()
        else -> actions.filterIsInstance<Artifact>()
    }.first { it.name == actionGoal.action.functor }

    override fun invoke(actionGoal: ActionGoal): Substitution = lookup(actionGoal).execute(actionGoal.value.args)
}
