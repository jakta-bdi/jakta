package it.unibo.jakta.kqml

import it.unibo.jakta.addAnnotations
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.replyAllTo
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.setAnnotations
import it.unibo.jakta.source
import it.unibo.tuprolog.core.toAtom

/**
 * Handles a KQML payload message and returns the corresponding agent update.
 * @param payload the payload of the message to handle
 * @return the corresponding agent update
 */
fun AgentState<PrologBelief, PrologGoal>.handleKQMLPayload(payload: KQMLPayload, sender: AgentID): AgentUpdate<*> =
    when (payload) {
        is Tell -> {
            val beliefs = payload.beliefs.map { it.addAnnotations(source(sender)) }
            AgentUpdate.Believe(beliefs)
        }

        // Of the beliefs matching the query, with the sender among their sources, only the sender's source is in scope:
        // with annotated beliefs, what others told stays.
        is Untell -> {
            val query = payload.beliefQuery.addAnnotations(source(sender))
            AgentUpdate.Replace<PrologBelief>(emptyList()) { belief ->
                belief.setAnnotations(source(sender)).takeIf { belief.matchBelief(query) != null }
            }
        }

        is Achieve -> {
            val goal = payload.goal.addAnnotations(source(sender))
            AgentUpdate.Adopt(goal)
        }

        is Unachieve -> {
            val goal = payload.goalQuery.addAnnotations(source(sender))
            AgentUpdate.Drop(goal)
        }

        is AskAll -> {
            val query = payload.query
            val id = payload.id.toString().toAtom()
            val goal = JaktaLogicProgrammingScope().replyAllTo(query, id).addAnnotations(source(sender))
            AgentUpdate.Adopt(goal)
        }

        is AskOne -> {
            val query = payload.query
            val id = payload.id.toString().toAtom()
            val goal = JaktaLogicProgrammingScope().replyOne(query, id).addAnnotations(source(sender))
            AgentUpdate.Adopt(goal)
        }
    }
