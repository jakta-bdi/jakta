package it.unibo.jakta.kqml

import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.replyAllTo
import it.unibo.jakta.dsl.goal.replyOneTo
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.event.AgentUpdate.Belief
import it.unibo.jakta.event.AgentUpdate.Goal
import it.unibo.jakta.source
import it.unibo.jakta.tag

/**
 * Handles a KQML payload message and returns the corresponding agent update.
 * @param message the message to handle
 * @return the corresponding agent update
 */
fun AgentState<PrologBelief, PrologGoal>.handleKQMLPayload(
    message: AgentEvent.External.Message<KQMLPayload>,
): AgentUpdate<*> = when (val payload = message.payload) {
    is Tell -> {
        val beliefs = payload.beliefs.map {
            it.tag(source(message.sender)) // TODO should I use something else for the source?
        }.toSet()
        Belief(beliefs)
    }

    is Untell -> {
        val belief = payload.belief.tag(source(message.sender))
        Belief(emptySet(), setOf(belief))
    }

    is Achieve -> {
        val goal = payload.goal.tag(source(message.sender))
        Goal(setOf(goal))
    }

    is Unachieve -> {
        val goal = payload.goal.tag(source(message.sender))
        Goal(emptySet(), setOf(goal))
    }

    is AskAll -> {
        val query = payload.query
        val id = message.payload.id
        val goal = replyAllTo(id) { query }.tag(source(message.sender))
        Goal(setOf(goal))
    }

    is AskOne -> {
        val query = (message.payload as AskOne).query
        val id = message.payload.id
        val goal = replyOneTo(id) { query }.tag(source(message.sender))
        Goal(setOf(goal))
    }
}
