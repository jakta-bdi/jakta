package io.github.anitvam.agents.bdi.goals.actions

import it.unibo.tuprolog.core.Substitution

interface Action {
    val id: ActionID
    val name: String
    fun execute(args: List<Any>): Substitution
}
