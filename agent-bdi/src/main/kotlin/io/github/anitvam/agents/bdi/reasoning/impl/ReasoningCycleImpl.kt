package io.github.anitvam.agents.bdi.reasoning.impl

import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.reasoning.EventSelectionFunction
import io.github.anitvam.agents.bdi.reasoning.PlanSelectionFunction
import io.github.anitvam.agents.bdi.reasoning.ReasoningCycle
import io.github.anitvam.agents.bdi.reasoning.SelectionFunction

class ReasoningCycleImpl: ReasoningCycle {

    override fun performReasoning(agentContext: AgentContext): AgentContext {
        // STEP1: Perceive the Environment
        val perceptions = agentContext.perception.percept()

        // STEP2: Update the BeliefBase
        val newBeliefBase = agentContext.buf(perceptions)

        // STEP3: Receiving Communication from Other Agents --> in futuro
        // STEP4: Selecting "Socially Acceptable" Messages --> in futuro

        // STEP5: Selecting an Event.
        val selectedEvent = EventSelectionFunction.simple().select(agentContext.events)
        val newEvents = agentContext.events - selectedEvent

        // STEP6: Retrieving all Relevant Plans.
        val relevantPlans = agentContext.planLibrary.relevantPlans(selectedEvent)
        // if the set of relevant plans is empty, the event is simply discarded.

        // STEP7: Determining the Applicable Plans.
        var applicablePlans : PlanLibrary
        if (relevantPlans.plans.isNotEmpty()) applicablePlans = relevantPlans.applicablePlans(agentContext.beliefBase)

        // STEP8: Selecting one Applicable Plan.

        // STEP9: Select an Intention for Further Execution.

        // STEP10: Executing one Step on an Intention


    }


}