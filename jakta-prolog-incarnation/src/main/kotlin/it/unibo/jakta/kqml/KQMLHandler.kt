package it.unibo.jakta.kqml

import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate

fun AgentState<PrologBelief, PrologGoal>.handleKQMLPayload(
    message : AgentEvent.External.Message<KQMLPayload>
) : AgentUpdate<*> {
    when(message.payload) {
        is tell -> {
            val belief = (message.payload as tell).belief
            return AgentUpdate.Belief(setOf(belief))
        }
        is untell -> {
            val belief = (message.payload as untell).belief
            return AgentUpdate.Belief(emptySet(), setOf(belief))
        }
        is achieve -> {
            val goal = (message.payload as achieve).goal
            return AgentUpdate.Goal(setOf(goal))
        }
        is unachieve -> {
            val goal = (message.payload as unachieve).goal
            return AgentUpdate.Goal(emptySet(), setOf(goal))
        }
    }
}

