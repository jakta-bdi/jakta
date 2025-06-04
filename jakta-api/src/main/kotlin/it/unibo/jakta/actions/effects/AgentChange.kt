package it.unibo.jakta.actions.effects

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.resolution.Matcher

fun interface AgentChange<Belief : Any, Query : Any, Response> :
    SideEffect,
    (Agent.Context.Mutable<Belief, Query, Response>) -> Unit

interface BeliefChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val belief: Belief

    data class Addition<Belief : Any, Query : Any, Response>(
        override val belief: Belief,
    ) : BeliefChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.beliefBase.add(belief)
        }
    }

    data class Removal<Belief : Any, Query : Any, Response>(
        override val belief: Belief,
    ) : BeliefChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.beliefBase.remove(belief)
        }
    }

    data class Update<Belief : Any, Query : Any, Response>(
        override val belief: Belief,
        val query: Query,
    ) : BeliefChange<Belief, Query, Response> {

        context(matcher: Matcher<Belief, Query, Response>)
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            val response = matcher.query(query, mutableAgentContext.beliefBase.snapshot())
            if (response != null) {
                mutableAgentContext.beliefBase.remove(response.deduce().first()) // TODO("How can it understand the type of Response I am extending in Matcher?")
                mutableAgentContext.beliefBase.add(belief)
            }
        }
    }
}


// TODO("If our new implementation is reactive, how do we communicate to the agent lifecycle that we want to drop an event?")
interface EventChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val event: Event.Internal.Goal<Belief, Query, Response>

    data class Addition<Belief : Any, Query : Any, Response>(
        override val event: Event.Internal.Goal<Belief, Query, Response>,
    ) : EventChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.enqueue(event)
        }
    }

    data class Removal<Belief : Any, Query : Any, Response>(
        override val event: Event.Internal.Goal<Belief, Query, Response>,
    ) : EventChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.drop(event)
        }
    }
}

interface IntentionChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val intention: Intention<Belief, Query, Response>

    data class Addition<Belief : Any, Query : Any, Response>(
        override val intention: Intention<Belief, Query, Response>,
    ) : IntentionChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.intentions.updateIntention(intention)
        }
    }

    data class Removal<Belief : Any, Query : Any, Response>(
        override val intention: Intention<Belief, Query, Response>,
    ) : IntentionChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.intentions.deleteIntention(intention.id)
        }
    }

    data class IntentionUpdate<Belief : Any, Query : Any, Response>(
        override val intention: Intention<Belief, Query, Response>,
    ) : IntentionChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.intentions.updateIntention(intention)
        }
    }
}

interface PlanChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val plan: Plan<Belief, Query, Response>

    data class Addition<Belief : Any, Query : Any, Response>(
        override val plan: Plan<Belief, Query, Response>,
    ) : PlanChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.plans.add(plan)
        }
    }

    data class Removal<Belief : Any, Query : Any, Response>(
        override val plan: Plan<Belief, Query, Response>,
    ) : PlanChange<Belief, Query, Response> {
        override fun invoke(mutableAgentContext: Agent.Context.Mutable<Belief, Query, Response>) {
            mutableAgentContext.plans.remove(plan)
        }
    }
}
