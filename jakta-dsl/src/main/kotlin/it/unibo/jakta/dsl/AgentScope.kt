package it.unibo.jakta.dsl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.InternalActions
import it.unibo.jakta.dsl.actions.InternalActionsScope
import it.unibo.jakta.dsl.beliefs.BeliefsScope
import it.unibo.jakta.dsl.goals.InitialGoalsScope
import it.unibo.jakta.dsl.plans.PlansScope
import it.unibo.jakta.events.Event
import it.unibo.jakta.executionstrategies.TimeDistribution
import it.unibo.jakta.executionstrategies.setTimeDistribution
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

class AgentScope(
    val name: String? = null,
) : Builder<ASAgent> {
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

    override fun build(): ASAgent {
        var agent = ASAgent.of(
            name = name.orEmpty(),
            beliefBase = beliefsScope.build(),
            events = goalsScope.build().map { Event.of(it) },
            planLibrary = PlanLibrary.of(plans + plansScope.build().toList()),
            internalActions = InternalActions.default() + actionsScope.build(),
        )
        if (this::time.isInitialized) {
            agent = agent.setTimeDistribution(time)
        }
        return agent
    }
}
