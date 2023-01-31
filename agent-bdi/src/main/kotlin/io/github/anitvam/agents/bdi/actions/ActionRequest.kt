package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import io.github.anitvam.agents.bdi.actions.impl.ExternalRequestImpl
import io.github.anitvam.agents.bdi.actions.impl.InternalRequestImpl
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

sealed interface ActionRequest<C : SideEffect, Res : ActionResponse<C>> {
    val arguments: List<Term>
    val requestTimestamp: Time

    fun reply(substitution: Substitution = Substitution.empty(), effects: Iterable<C>): Res

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: C): Res
}

interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    val agent: Agent

    companion object {
        fun of(agent: Agent, arguments: Iterable<Term>): InternalRequest =
            InternalRequestImpl(agent, arguments.toList())

        fun of(agent: Agent, vararg arguments: Term): InternalRequest =
            of(agent, arguments.asList())
    }
}

interface ExternalRequest : ActionRequest<EnvironmentChange, ExternalResponse> {
    val environment: Environment
    val sender: String

    companion object {
        fun of(environment: Environment, sender: String, arguments: Iterable<Term>): ExternalRequest =
            ExternalRequestImpl(environment, arguments.toList(), sender)

        fun of(environment: Environment, sender: String, vararg arguments: Term): ExternalRequest =
            ExternalRequestImpl(environment, arguments.asList(), sender)
    }
}
