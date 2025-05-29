package it.unibo.jakta.actions.effects

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution

fun interface AgentChange : SideEffect, (ASAgent.ASMutableAgentContext) -> Unit

interface BeliefChange : AgentChange {
    val belief: ASBelief

    data class BeliefAddition(override val belief: ASBelief) : BeliefChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.add(belief)
        }
    }

    data class BeliefRemoval(override val belief: ASBelief) : BeliefChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.remove(belief)
        }
    }

    data class BeliefUpdate(override val belief: ASBelief) : BeliefChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.beliefBase.update(belief)
        }
    }
}

interface EventChange : AgentChange {
    val event: Event.Internal.Goal<ASBelief, Struct, Solution>

    data class EventAddition(override val event: Event.Internal.Goal<ASBelief, Struct, Solution>) : EventChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.enqueue(event)
        }
    }

    data class EventRemoval(override val event: Event.Internal.Goal<ASBelief, Struct, Solution>) : EventChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.drop(event)
        }
    }
}

interface IntentionChange : AgentChange {
    val intention: ASIntention

    data class IntentionAddition(override val intention: ASIntention) : IntentionChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.intentions.updateIntention(intention)
        }
    }

    data class IntentionRemoval(override val intention: ASIntention) : IntentionChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.intentions.deleteIntention(intention.id)
        }
    }

    data class IntentionUpdate(override val intention: ASIntention) : IntentionChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.intentions.updateIntention(intention).also { "Updated" }
        }
    }
}

interface PlanChange : AgentChange {
    val plan: Plan<ASBelief, Struct, Solution>

    data class PlanAddition(override val plan: Plan<ASBelief, Struct, Solution>) : PlanChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.plans.add(plan)
        }
    }

    data class PlanRemoval(override val plan: Plan<ASBelief, Struct, Solution>) : PlanChange {
        override fun invoke(mutableAgentContext: ASAgent.ASMutableAgentContext) {
            mutableAgentContext.plans.remove(plan)
        }
    }
}
