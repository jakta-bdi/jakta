package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.actions.InternalActions
import io.github.anitvam.agents.bdi.dsl.actions.InternalActionsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.goals.InitialGoalsScope
import io.github.anitvam.agents.bdi.dsl.plans.PlansScope
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.TimeDistribution
import io.github.anitvam.agents.bdi.executionstrategies.setTimeDistribution
import io.github.anitvam.agents.bdi.executionstrategies.timeDistribution
import io.github.anitvam.agents.bdi.plans.PlanLibrary

class AgentScope(
    val name: String? = null,
) : Builder<Agent> {
    private val beliefsScope by lazy { BeliefsScope() }
    private val goalsScope by lazy { InitialGoalsScope() }
    private val plansScope by lazy { PlansScope() }
    private val actionsScope by lazy { InternalActionsScope() }

    private lateinit var time: TimeDistribution

    fun beliefs(f: BeliefsScope.() -> Unit): AgentScope {
        beliefsScope.also(f)
        return this
    }

    fun goals(f: InitialGoalsScope.() -> Unit): AgentScope {
        goalsScope.also(f)
        return this
    }

    fun plans(f: PlansScope.() -> Unit): AgentScope {
        plansScope.also(f)
        return this
    }

    fun actions(f: InternalActionsScope.() -> Unit): AgentScope {
        actionsScope.also(f)
        return this
    }

    fun timeDistribution(timeDistribution: TimeDistribution): AgentScope {
        this.time = timeDistribution
        return this
    }

    override fun build(): Agent {
        var agent = Agent.of(
            name = name.orEmpty(),
            beliefBase = beliefsScope.build(),
            events = goalsScope.build().map { Event.of(it) },
            planLibrary = PlanLibrary.of(plansScope.build().toList()),
            internalActions = InternalActions.default() + actionsScope.build()
        )
        if (this::time.isInitialized) {
            agent = agent.setTimeDistribution(time)
        }
        return agent
    }
}
