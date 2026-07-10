package it.unibo.jakta.kqml

import it.unibo.jakta.JAKTA_ANNOTATIONS_TAG
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.utils.setTag

/**
 * Handles a KQML payload message and returns the corresponding agent update.
 * @param message the message to handle
 * @return the corresponding agent update
 */
fun AgentState<PrologBelief, PrologGoal>.handleKQMLPayload(
    message: AgentEvent.External.Message<KQMLPayload>,
): AgentUpdate<*> = when (message.payload) {
    is Tell -> {
        val belief = (message.payload as Tell).belief
            .setTag(
                JAKTA_ANNOTATIONS_TAG,
                setOf(Struct.of("source", Atom.of(message.sender.displayName))),
            )
        AgentUpdate.Belief(setOf(belief))
    }

    is Untell -> {
        val belief = (message.payload as Untell).belief
        AgentUpdate.Belief(emptySet(), setOf(belief))
    }

    is Achieve -> {
        val goal = (message.payload as Achieve).goal
        AgentUpdate.Goal(setOf(goal))
    }

    is Unachieve -> {
        val goal = (message.payload as Unachieve).goal
        AgentUpdate.Goal(emptySet(), setOf(goal))
    }
}
