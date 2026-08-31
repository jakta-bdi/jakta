package it.unibo.jakta.kqml

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.allSolutionsOf
import it.unibo.jakta.logic.annotationUnificator
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith
import kotlin.time.Duration
import kotlin.uuid.Uuid

// TODO do we like this or not? I started implementing them for the ask-and-wait semantic
//  and ended up implementing shortcuts for all performatives

// TODO is this something like a "KQMLMessagingSkill"? Why? Why not?
//  does it make sense to even separate a tell/untell skill from a achieve/unachieve skill?
//  the difference would be that without having it as a skill we can simply equip the agent with the `MessagingSkill`

context(skill: MessagingSkill)
private fun <P : KQMLPayload> Agent.kqmlSend(receiver: AgentID, payload: P) {
    with(skill) {
        sendTo(receiver, payload)
    }
}

context(skill: MessagingSkill)
private fun <P : KQMLPayload> Agent.kqmlBroadcast(payload: P) {
    with(skill) {
        broadcast(payload)
    }
}

/**
 * Extension function to send a message using the [Tell] performative.
 */
context(skill: MessagingSkill)
fun Agent.tellTo(receiver: AgentID, vararg belief: PrologBelief) = kqmlSend(receiver, Tell(belief.toList()))

/**
 * Extension function to send a message using the [Tell] performative.
 */
context(skill: MessagingSkill)
fun Agent.tellTo(receiver: AgentID, replyingTo: String, vararg belief: PrologBelief) =
    kqmlSend(receiver, Tell(belief.toList(), Uuid.parse(replyingTo)))

/**
 * Extension function to broadcast a message using the [Tell] performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastTell(vararg belief: PrologBelief) = kqmlBroadcast(Tell(belief.toList()))

/**
 * Extension function to send a message using [Untell] performative.
 */
context(skill: MessagingSkill)
fun Agent.untellTo(receiver: AgentID, belief: PrologBelief) = kqmlSend(receiver, Untell(belief))

/**
 * Extension function to use the [Untell] performative to broadcast a message.
 */
context(skill: MessagingSkill)
fun Agent.broadcastUntell(belief: PrologBelief) = kqmlBroadcast(Untell(belief))

/**
 * Extension function to send a message using the [Achieve] performative.
 */
context(skill: MessagingSkill)
fun Agent.delegateAchieveTo(receiver: AgentID, goal: PrologGoal) = kqmlSend(receiver, Achieve(goal))

/**
 * Extension function to broadcast a message using the [Achieve] performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastAchieve(goal: PrologGoal) = kqmlBroadcast(Achieve(goal))

/**
 * Extension function to send a message using the [Unachieve] performative.
 */
context(skill: MessagingSkill)
fun Agent.sendUnachieveTo(receiver: AgentID, goal: PrologGoal) = kqmlSend(receiver, Unachieve(goal))

/**
 * Extension function to broadcast a message using the [Unachieve] performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastUnachieve(goal: PrologGoal) = kqmlBroadcast(Unachieve(goal))

/**
 * Extension function to send a message using the [AskOne] performative and wait for a reply.
 * the agent will wait for [timeout] duration for a reply from the [receiver]
 * that matches the id of the question.
 * If a matching reply is received the substitution of the payload is applied to the [planContext] and returned.
 * If no matching reply is received or the timeout is reached `null` is returned.
 */
context(skill: MessagingSkill, planContext: MutableSubstitutionPlanContext)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askOneTo(
    receiver: AgentID,
    query: Fact,
    timeout: Duration? = null,
): Substitution? {
    val message = AskOne(query)
    kqmlSend(receiver, message)
    val eventFilter: (AgentEvent) -> Substitution? = { event ->
        when (event) {
            is AgentEvent.External.Message<*> -> when (val payload = event.payload) {
                is Tell -> {
                    if (event.sender != receiver || payload.replyingTo != message.id || payload.beliefs.isEmpty()) {
                        null
                    } else {
                        val substitution = annotationUnificator.mgu(payload.beliefs.first(), query)
                        if (substitution.isSuccess) {
                            planContext += substitution
                        }
                        substitution
                    }
                }

                else -> null
            }

            else -> null
        }
    }
    return this.wait(eventFilter, timeout)
}

/**
 * Extension function to send a message using the [AskAll] performative and wait for a reply.
 * The agent will wait for the [timeout] duration for a reply from the [receiver] that matches the id of the question.
 * The reply should be a [Tell] message with a [List] of beliefs that unify with the [query].
 * If a matching reply is received, the a [List] of [Substitution]s is returned.
 * If no matching reply is received or the timeout is reached `null` is returned.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askAllTo(
    receiver: AgentID,
    query: Fact,
    timeout: Duration? = null,
): List<Substitution>? {
    val message = AskOne(query)
    kqmlSend(receiver, message)
    val eventFilter: (AgentEvent) -> List<Substitution>? = { event ->
        when (event) {
            is AgentEvent.External.Message<*> -> when (val payload = event.payload) {
                is Tell -> {
                    if (payload.replyingTo != message.id) {
                        null
                    } else {
                        payload.beliefs.allSolutionsOf(query).map { it.substitution }.filter { it.isSuccess }
                    }
                }

                else -> null
            }

            else -> null
        }
    }
    return this.wait(eventFilter, timeout)
}

// TODO should we even allow broadcasting `ask` performatives?
//  What happens if more than one agent replies?
//  How should we handle this?
//  How do we know how many reply to wait for? We could have that only the first is waited upon.
//  but what happens to the others?

// /**
// * Extension function to broadcast a message using the `askOne` performative and wait for a reply.
// */
// context(skill: MessagingSkill)
// suspend fun MutableAgentState<PrologBelief, PrologGoal>.broadcastAskOne(
//    payload: AskOne,
//    timeout: Duration? = null,
// ): Substitution? {
//    kqmlBroadcast(payload)
//    val eventFilter = TODO()
//    return this.wait(eventFilter, timeout)
// }
//
// /**
// * Extension function to broadcast a message using the `askAll` performative and wait for the earliest set of replies.
// */
// context(skill: MessagingSkill)
// suspend fun MutableAgentState<PrologBelief, PrologGoal>.broadcastAskAll(
//    payload: AskAll,
//    timeout: Duration? = null,
// ): List<Substitution>? {
//    with(skill) {
//        broadcast(payload)
//    }
//    val eventFilter = TODO()
//    return this.wait(eventFilter, timeout)
// }
