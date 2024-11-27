package it.unibo.jakta.actions.effects

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan

interface AgentChange : ActionResult {
    fun ASMutableAgentContext.apply(controller: Activity.Controller?)
}

interface BeliefChange: AgentChange {
    val belief: ASBelief

    class BeliefAddition(override val belief: ASBelief): BeliefChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutableBeliefBase.add(belief)
            mutableEventList.add(BeliefBaseAddition(belief))
            mutableBeliefBase.delta = emptyList() //TODO("Perhaps is better to remove deltas from BB?")
        }
    }

    class BeliefRemoval(override val belief: ASBelief): BeliefChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutableBeliefBase.remove(belief)
            mutableEventList.add(BeliefBaseRemoval(belief))
            mutableBeliefBase.delta = emptyList()
        }
    }
}

interface IntentionChange : AgentChange {
    val intention: ASIntention

    class IntentionAddition(override val intention: ASIntention): IntentionChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutableIntentionPool.updateIntention(intention)
        }
    }

    class IntentionRemoval(override val intention: ASIntention): IntentionChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutableIntentionPool.deleteIntention(intention.id)
        }
    }
}

interface EventChange : AgentChange {
    val event: ASEvent

    class EventAddition(override val event: ASEvent): EventChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutableEventList.add(event)
        }
    }

    class EventRemoval(override val event: ASEvent): EventChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutableEventList.remove(event)
        }
    }
}

interface PlanChange : AgentChange {
    val plan: ASPlan

    class PlanAddition(override val plan: ASPlan) : PlanChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutablePlanLibrary.add(plan)
        }
    }

    class PlanRemoval(override val plan: ASPlan) : PlanChange {
        override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
            mutablePlanLibrary.remove(plan)
        }
    }
}

class Sleep(val millis: Long) : AgentChange {
    override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
        controller?.sleep(millis)
    }
}

object Stop : AgentChange {
    override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
        controller?.stop()
    }
}

object Pause : AgentChange {
    override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
        controller?.pause()
    }
}
