package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.dsl.actions.InternalActionsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.goals.InitialGoalsScope
import io.github.anitvam.agents.bdi.dsl.plans.PlansScope
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

class AgentScope(
    val name: String? = null,
) : Builder<Agent> {
    private var beliefs: Iterable<Belief> = emptyList()
    private var events: Iterable<Event> = emptyList()
    private var plans: Iterable<Plan> = emptyList()

    private val beliefsScope by lazy { BeliefsScope() }
    private val goalsScope by lazy { InitialGoalsScope() }
    private val plansScope by lazy { PlansScope() }
    private val actionsScope by lazy { InternalActionsScope() }

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

    override fun build(): Agent = Agent.of(
        name = name.orEmpty(),
        beliefBase = beliefsScope.build(),
        events = goalsScope.build().map { Event.of(it) },
        planLibrary = PlanLibrary.of(plansScope.build().toList()),
    )
}
