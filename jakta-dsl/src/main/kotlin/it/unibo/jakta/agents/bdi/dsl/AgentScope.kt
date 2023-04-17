package it.unibo.jakta.agents.bdi.dsl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.actions.InternalActions
import it.unibo.jakta.agents.bdi.dsl.actions.InternalActionsScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.BeliefsScope
import it.unibo.jakta.agents.bdi.dsl.goals.InitialGoalsScope
import it.unibo.jakta.agents.bdi.dsl.plans.PlansScope
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.TimeDistribution
import it.unibo.jakta.agents.bdi.executionstrategies.setTimeDistribution
import it.unibo.jakta.agents.bdi.executionstrategies.timeDistribution
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

class AgentScope(
    val name: String? = null,
) : Builder<Agent> {
    private val beliefsScope by lazy { BeliefsScope() }
    private val goalsScope by lazy { InitialGoalsScope() }
    private val plansScope by lazy { PlansScope() }
    private val actionsScope by lazy { InternalActionsScope() }
    private var plans = emptyList<Plan>()
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

    fun plans(plansList: Iterable<Plan>): AgentScope {
        plans = plans + plansList
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
            planLibrary = PlanLibrary.of(plans + plansScope.build().toList()),
            internalActions = InternalActions.default() + actionsScope.build()
        )
        if (this::time.isInitialized) {
            agent = agent.setTimeDistribution(time)
        }
        return agent
    }
}
