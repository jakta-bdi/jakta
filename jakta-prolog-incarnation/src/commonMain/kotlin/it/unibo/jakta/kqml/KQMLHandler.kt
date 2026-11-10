package it.unibo.jakta.kqml

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.belief.withAnnotations
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.replyAllTo
import it.unibo.jakta.dsl.goal.replyOne
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.source
import it.unibo.jakta.tag
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.toAtom

/**
 * Handles a KQML payload message and returns the corresponding agent update.
 * @param payload the payload of the message to handle
 * @return the corresponding agent update
 */
fun AgentState<PrologBelief, PrologGoal>.handleKQMLPayload(payload: KQMLPayload, sender: AgentID): AgentUpdate<*> =
    when (payload) {
        is Tell -> {
            val beliefs = payload.beliefs.map { Fact.of(it.head.tag(source(sender))) }
            AgentUpdate.Believe(beliefs)
        }

        // Of the beliefs matching the query, with the sender among their sources, only the sender's source is in scope:
        // with annotated beliefs, what others told stays.
        is Untell -> {
            val query = payload.beliefQuery.tag(source(sender))
            AgentUpdate.Replace<PrologBelief>(
                { belief ->
                    belief.withAnnotations(setOf(source(sender))).takeIf { belief.matchBelief(query) != null }
                },
                emptyList(),
            )
        }

        is Achieve -> {
            val goal = payload.goal.tag(source(sender))
            AgentUpdate.Adopt(goal)
        }

        is Unachieve -> {
            val goal = payload.goalQuery.tag(source(sender))
            AgentUpdate.Drop(goal)
        }

        is AskAll -> {
            val query = payload.query
            val id = payload.id.toString().toAtom()
            val goal = JaktaLogicProgrammingScope().replyAllTo(query, id).tag(source(sender))
            AgentUpdate.Adopt(goal)
        }

        is AskOne -> {
            val query = payload.query
            val id = payload.id.toString().toAtom()
            val goal = JaktaLogicProgrammingScope().replyOne(query, id).tag(source(sender))
            AgentUpdate.Adopt(goal)
        }
    }
