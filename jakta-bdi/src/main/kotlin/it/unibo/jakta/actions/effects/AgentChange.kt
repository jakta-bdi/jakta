package it.unibo.jakta.actions.effects

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan

fun interface AgentChange : ActionSideEffect, (ASMutableAgentContext) -> Unit

interface BeliefChange: AgentChange {
    val belief: ASBelief

    class BeliefAddition(override val belief: ASBelief): BeliefChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            with(mutableAgentContext) {
                mutableBeliefBase.add(belief)
                mutableEventList.add(BeliefBaseAddition(belief))
                mutableBeliefBase.delta = emptyList() //TODO("Perhaps is better to remove deltas from BB?")
            }
        }
    }

    class BeliefRemoval(override val belief: ASBelief): BeliefChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            with(mutableAgentContext) {
                mutableBeliefBase.remove(belief)
                mutableEventList.add(BeliefBaseRemoval(belief))
                mutableBeliefBase.delta = emptyList()
            }
        }
    }
}

interface IntentionChange : AgentChange {
    val intention: ASIntention

    class IntentionAddition(override val intention: ASIntention): IntentionChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            mutableAgentContext.mutableIntentionPool.updateIntention(intention)
        }
    }

    class IntentionRemoval(override val intention: ASIntention): IntentionChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            mutableAgentContext.mutableIntentionPool.deleteIntention(intention.id)
        }
    }
}

interface EventChange : AgentChange {
    val event: ASEvent

    class EventAddition(override val event: ASEvent): EventChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            mutableAgentContext.mutableEventList.add(event)
        }
    }

    class EventRemoval(override val event: ASEvent): EventChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            mutableAgentContext.mutableEventList.remove(event)
        }
    }
}

interface PlanChange : AgentChange {
    val plan: ASPlan

    class PlanAddition(override val plan: ASPlan) : PlanChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            mutableAgentContext.mutablePlanLibrary.add(plan)
        }
    }

    class PlanRemoval(override val plan: ASPlan) : PlanChange {
        override fun invoke(mutableAgentContext: ASMutableAgentContext) {
            mutableAgentContext.mutablePlanLibrary.remove(plan)
        }
    }
}
