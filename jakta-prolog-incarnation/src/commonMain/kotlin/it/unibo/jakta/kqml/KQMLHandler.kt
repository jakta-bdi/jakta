package it.unibo.jakta.kqml

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.replyAllTo
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.event.AgentUpdate.Belief
import it.unibo.jakta.event.AgentUpdate.Goal
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.source
import it.unibo.jakta.tag
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.toAtom

/**
 * Handles a KQML payload message and returns the corresponding agent update.
 * @param payload the payload of the message to handle
 * @return the corresponding agent update
 */
fun AgentState<PrologBelief, PrologGoal>.handleKQMLPayload(payload: KQMLPayload, sender: AgentID): AgentUpdate<*> =
    when (payload) {
        is Tell -> {
            val beliefs = payload.beliefs.map { Fact.of(it.head.tag(source(sender))) }.toSet()
            Belief(beliefs)
        }

        is Untell -> {
            val query = payload.beliefQuery.tag(source(sender))
            val toRemove = beliefs.filter {
                it.matchBelief(query) != null
            }.toSet()
            Belief(emptySet(), toRemove)
        }

        is Achieve -> {
            val goal = payload.goal.tag(source(sender))
            Goal(setOf(goal))
        }

        // As in Jason, unachieve drops every matching goal, whoever adopted it.
        // The query goes first: for non-fact terms the matcher takes the second term as the annotated one.
        is Unachieve -> {
            val toRemove = goals.filter { payload.goalQuery.annotatedMguWith(it) !is Substitution.Fail }.toSet()
            Goal(emptySet(), toRemove)
        }

        is AskAll -> {
            val query = payload.query
            val id = payload.id.toString().toAtom()
            val goal = JaktaLogicProgrammingScope().replyAllTo(query, id).tag(source(sender))
            Goal(setOf(goal))
        }

        is AskOne -> {
            val query = payload.query
            val id = payload.id.toString().toAtom()
            val goal = JaktaLogicProgrammingScope().replyOne(query, id).tag(source(sender))
            Goal(setOf(goal))
        }
    }
