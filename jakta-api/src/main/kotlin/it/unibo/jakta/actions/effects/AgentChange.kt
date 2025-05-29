package it.unibo.jakta.actions.effects

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.Plan

fun interface AgentChange<Belief : Any, Query : Any, Response> :
    SideEffect,
    (Agent<Belief, Query, Response>.ASMutableAgentContext) -> Unit

interface BeliefChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val belief: Belief

    data class BeliefAddition<Belief : Any, Query : Any, Response>(override val belief: Belief) : BeliefChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.add(belief)
        }
    }

    data class BeliefRemoval<Belief : Any, Query : Any, Response>(override val belief: Belief) : BeliefChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.remove(belief)
        }
    }

    data class BeliefUpdate<Belief : Any, Query : Any, Response>(override val belief: Belief) : BeliefChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.update(belief)
        }
    }
}

interface EventChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val event: Event.Internal.Goal<Belief, Query, Response>

    data class EventAddition<Belief : Any, Query : Any, Response>(
        override val event: Event.Internal.Goal<Belief, Query, Response>,
    ) : EventChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.enqueue(event)
        }
    }

    data class EventRemoval<Belief : Any, Query : Any, Response>(
        override val event: Event.Internal.Goal<Belief, Query, Response>,
    ) : EventChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.drop(event)
        }
    }
}

interface IntentionChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val intention: Intention<Belief, Query, Response>

    data class IntentionAddition<Belief : Any, Query : Any, Response>(override val intention: ASIntention) :
        IntentionChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.intentions.updateIntention(intention)
        }
    }

    data class IntentionRemoval<Belief : Any, Query : Any, Response>(override val intention: ASIntention) :
        IntentionChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.intentions.deleteIntention(intention.id)
        }
    }

    data class IntentionUpdate<Belief : Any, Query : Any, Response>(override val intention: ASIntention) :
        IntentionChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.intentions.updateIntention(intention).also { "Updated" }
        }
    }
}

interface PlanChange<Belief : Any, Query : Any, Response> : AgentChange<Belief, Query, Response> {
    val plan: Plan<Belief, Query, Response>

    data class PlanAddition<Belief : Any, Query : Any, Response>(override val plan: Plan<Belief, Query, Response>) :
        PlanChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.plans.add(plan)
        }
    }

    data class PlanRemoval<Belief : Any, Query : Any, Response>(override val plan: Plan<Belief, Query, Response>) :
        PlanChange {
        override fun invoke(mutableAgentContext: Agent<Belief, Query, Response>.ASMutableAgentContext) {
            mutableAgentContext.plans.remove(plan)
        }
    }
}
