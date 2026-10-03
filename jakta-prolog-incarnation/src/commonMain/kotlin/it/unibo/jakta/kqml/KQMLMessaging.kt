package it.unibo.jakta.kqml

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.allSolutionsOf
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
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
fun Agent.tellTo(receiver: AgentID, vararg belief: PrologBelief) = kqmlSend(receiver, Tell(belief.toSet()))

/**
 * Extension function to reply to the question with id [replyingTo], sent by [receiver], with the [Reply] performative.
 */
context(skill: MessagingSkill)
fun Agent.replyTo(receiver: AgentID, replyingTo: String, vararg belief: PrologBelief) =
    kqmlSend(receiver, Reply(belief.toSet(), Uuid.parse(replyingTo)))

/**
 * Extension function to broadcast a message using the [Tell] performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastTell(vararg belief: PrologBelief) = kqmlBroadcast(Tell(belief.toSet()))

/**
 * Extension function to send a message using [Untell] performative.
 */
context(skill: MessagingSkill)
fun Agent.untellTo(receiver: AgentID, query: Struct) = kqmlSend(receiver, Untell(query))

/**
 * Extension function to use the [Untell] performative to broadcast a message.
 */
context(skill: MessagingSkill)
fun Agent.broadcastUntell(query: Struct) = kqmlBroadcast(Untell(query))

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
fun Agent.sendUnachieveTo(receiver: AgentID, query: Struct) = kqmlSend(receiver, Unachieve(query))

/**
 * Extension function to broadcast a message using the [Unachieve] performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastUnachieve(query: Struct) = kqmlBroadcast(Unachieve(query))

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
    query: Struct,
    timeout: Duration? = null,
): Substitution? {
    val message = AskOne(query)
    kqmlSend(receiver, message)
    val eventFilter: (AgentEvent) -> Substitution? = { event ->
        when (event) {
            is AgentEvent.External.Message<*> -> when (val payload = event.payload) {
                is Reply -> {
                    if (event.sender != receiver || payload.replyingTo != message.id || payload.beliefs.isEmpty()) {
                        null
                    } else {
                        val substitution = payload.beliefs.first().annotatedMguWith(query)
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
 * The reply should be a [Reply] message with a [List] of beliefs that unify with the [query].
 * If a matching reply is received, the a [List] of [Substitution]s is returned.
 * If no matching reply is received or the timeout is reached `null` is returned.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askAllTo(
    receiver: AgentID,
    query: Struct,
    timeout: Duration? = null,
): List<Substitution>? {
    val message = AskAll(query)
    kqmlSend(receiver, message)
    val eventFilter: (AgentEvent) -> List<Substitution>? = { event ->
        when (event) {
            is AgentEvent.External.Message<*> -> when (val payload = event.payload) {
                is Reply -> {
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

/**
 * Extension function to send a message using the [AskOne] performative to every one of the [receivers],
 * and wait for each of them to reply.
 * Waits at most [timeout], if given, then returns the replies received so far.
 * @return the [Substitution] of the [query] with the reply of each receiver that answered.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askOneTo(
    receivers: Collection<AgentID>,
    query: Struct,
    timeout: Duration? = null,
): Map<AgentID, Substitution> {
    val message = AskOne(query)
    receivers.forEach { kqmlSend(it, message) }
    return waitReplies(message.id, receivers.toSet().size, timeout) { answerOne(query) }
}

/**
 * Extension function to send a message using the [AskAll] performative to every one of the [receivers],
 * and wait for each of them to reply.
 * Waits at most [timeout], if given, then returns the replies received so far.
 * @return the [Substitution]s of the [query] with the reply of each receiver that answered.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askAllTo(
    receivers: Collection<AgentID>,
    query: Struct,
    timeout: Duration? = null,
): Map<AgentID, List<Substitution>> {
    val message = AskAll(query)
    receivers.forEach { kqmlSend(it, message) }
    return waitReplies(message.id, receivers.toSet().size, timeout) { answerAll(query) }
}

/**
 * Extension function to broadcast a message using the [AskOne] performative, and wait for the first [replies].
 * As the number of agents that will reply is not known, it waits at most [timeout],
 * then returns the replies received so far.
 * @return the [Substitution] of the [query] with the reply of each agent that answered.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.broadcastAskOne(
    query: Struct,
    timeout: Duration,
    replies: Int = 1,
): Map<AgentID, Substitution> {
    val message = AskOne(query)
    kqmlBroadcast(message)
    return waitReplies(message.id, replies, timeout) { answerOne(query) }
}

/**
 * Extension function to broadcast a message using the [AskAll] performative, and wait for the first [replies].
 * As the number of agents that will reply is not known, it waits at most [timeout],
 * then returns the replies received so far.
 * @return the [Substitution]s of the [query] with the reply of each agent that answered.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.broadcastAskAll(
    query: Struct,
    timeout: Duration,
    replies: Int = 1,
): Map<AgentID, List<Substitution>> {
    val message = AskAll(query)
    kqmlBroadcast(message)
    return waitReplies(message.id, replies, timeout) { answerAll(query) }
}

private fun Reply.answerOne(query: Struct): Substitution? =
    beliefs.firstOrNull()?.annotatedMguWith(query)?.takeIf { it.isSuccess }

private fun Reply.answerAll(query: Struct): List<Substitution> =
    beliefs.allSolutionsOf(query).map { it.substitution }.filter { it.isSuccess }

/**
 * Waits for the replies to the question [questionId], keeping the [answer] of the first reply of each sender,
 * until [replies] senders answered or the [timeout] expires.
 * @return the answers received, by sender.
 */
private suspend fun <T : Any> MutableAgentState<PrologBelief, PrologGoal>.waitReplies(
    questionId: Uuid,
    replies: Int,
    timeout: Duration?,
    answer: Reply.() -> T?,
): Map<AgentID, T> {
    if (replies <= 0) return emptyMap()
    val answers = mutableMapOf<AgentID, T>()
    val eventFilter: (AgentEvent) -> Map<AgentID, T>? = { event ->
        val payload = (event as? AgentEvent.External.Message<*>)?.payload
        if (payload is Reply && payload.replyingTo == questionId && answers.size < replies) {
            payload.answer()?.let { answers.getOrPut(event.sender) { it } }
        }
        answers.toMap().takeIf { it.size >= replies }
    }
    return wait(eventFilter, timeout) ?: answers.toMap()
}
