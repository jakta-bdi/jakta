package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.ActionGoal
import io.github.anitvam.agents.bdi.goals.actions.impl.ActionLibraryImpl
import it.unibo.tuprolog.core.Substitution

interface ActionLibrary {
    val actions: List<Action>

    fun lookup(actionGoal: ActionGoal): Action

    fun invoke(actionGoal: ActionGoal): Substitution

    companion object {
        fun default(): ActionLibrary = ActionLibraryImpl()
    }
}
