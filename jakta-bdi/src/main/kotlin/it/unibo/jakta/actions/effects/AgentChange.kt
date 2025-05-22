package it.unibo.jakta.actions.effects

import it.unibo.jakta.ASAgent
import it.unibo.jakta.Agent
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan

fun interface AgentChange : SideEffect, (ASAgent.ASMutableAgentContext) -> Unit

interface BeliefChange : AgentChange {
    val belief: ASBelief

    class BeliefAddition(override val belief: ASBelief) : BeliefChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.add(belief)
        }
    }

    class BeliefRemoval(override val belief: ASBelief) : BeliefChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.remove(belief)
        }
    }
}

interface EventChange : AgentChange {
    val event: Event.AgentEvent

    class EventAddition(override val event: Event.AgentEvent) : EventChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.events.add(event)
        }
    }

    class EventRemoval(override val event: Event.AgentEvent) : EventChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.events.remove(event)
        }
    }
}

interface IntentionChange : AgentChange {
    val intention: ASIntention

    class IntentionAddition(override val intention: ASIntention) : IntentionChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.intentions.updateIntention(intention)
        }
    }

    class IntentionRemoval(override val intention: ASIntention) : IntentionChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.intentions.deleteIntention(intention.id)
        }
    }
}

interface PlanChange : AgentChange {
    val plan: ASPlan

    class PlanAddition(override val plan: ASPlan) : PlanChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.plans.add(plan)
        }
    }

    class PlanRemoval(override val plan: ASPlan) : PlanChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.plans.remove(plan)
        }
    }
}
