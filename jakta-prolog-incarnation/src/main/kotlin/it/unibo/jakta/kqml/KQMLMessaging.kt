package it.unibo.jakta.kqml

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution
import kotlin.time.Duration

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
 * Extension function to send a message using the `tell` performative.
 */
context(skill: MessagingSkill)
fun Agent.tellTo(receiver: AgentID, belief: PrologBelief) = kqmlSend(receiver, Tell(belief))

/**
 * Extension function to broadcast a message using the `tell` performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastTell(belief: PrologBelief) = kqmlBroadcast(Tell(belief))

/**
 * Extension function to send a message using `untell` performative.
 */
context(skill: MessagingSkill)
fun Agent.untellTo(receiver: AgentID, belief: PrologBelief) = kqmlSend(receiver, Untell(belief))

/**
 * Extension function to use the `untell` performative to broadcast a message.
 */
context(skill: MessagingSkill)
fun Agent.broadcastUntell(belief: PrologBelief) = kqmlBroadcast(Untell(belief))

/**
 * Extension function to send a message using the `achieve` performative.
 */
context(skill: MessagingSkill)
fun Agent.delegateAchieveTo(receiver: AgentID, goal: PrologGoal) = kqmlSend(receiver, Achieve(goal))

/**
 * Extension function to broadcast a message using the `achieve` performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastAchieve(goal: PrologGoal) = kqmlBroadcast(Achieve(goal))

/**
 * Extension function to send a message using the `unachieve` performative.
 */
context(skill: MessagingSkill)
fun Agent.sendUnachieveTo(receiver: AgentID, goal: PrologGoal) = kqmlSend(receiver, Unachieve(goal))

/**
 * Extension function to broadcast a message using the `unachieve` performative.
 */
context(skill: MessagingSkill)
fun Agent.broadcastUnachieve(goal: PrologGoal) = kqmlBroadcast(Unachieve(goal))

// TODO what should these function return?? --> Substitution or List of Substitution

// TODO do we have a way to modify the original substitution to include the newly substituted variables from the query?
//  if not, how do we deal with this? Can we create a "MutableSubstitution"?
//  does it make sense though? How would I deal with the AskALL?
//  Probably working with the substitution manually is the way, but you must know that variables are not substituted.

/**
 * Extension function to send a message using the `askOne` performative and wait for a reply.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askOneTo(
    receiver: AgentID,
    query: Struct,
    timeout: Duration? = null,
): Substitution? {
    kqmlSend(receiver, AskOne(query))
    val eventFilter: (AgentEvent) -> Substitution? = TODO()
    return this.wait(eventFilter, timeout)
}

/**
 * Extension function to send a message using the `askAll` performative and wait for a reply.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.askAllTo(
    receiver: AgentID,
    query: Struct,
    timeout: Duration? = null,
): List<Substitution>? {
    kqmlSend(receiver, AskAll(query))
    val eventFilter = TODO()
    return this.wait(eventFilter, timeout)
}

// TODO should we even allow broadcasting `ask` performatives?
//  What happens if more than one agent replies?
//  How should we handle this?
//  How do we know how many reply to wait for? At the moment only the first one is waited upon,
//  but what happens to the others?

/**
 * Extension function to broadcast a message using the `askOne` performative and wait for a reply.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.broadcastAskOne(
    payload: AskOne,
    timeout: Duration? = null,
): Substitution? {
    kqmlBroadcast(payload)
    val eventFilter = TODO()
    return this.wait(eventFilter, timeout)
}

/**
 * Extension function to broadcast a message using the `askAll` performative and wait for the earliest set of replies.
 */
context(skill: MessagingSkill)
suspend fun MutableAgentState<PrologBelief, PrologGoal>.broadcastAskAll(
    payload: AskAll,
    timeout: Duration? = null,
): List<Substitution>? {
    with(skill) {
        broadcast(payload)
    }
    val eventFilter = TODO()
    return this.wait(eventFilter, timeout)
}
