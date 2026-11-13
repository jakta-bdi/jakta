package it.unibo.jakta.event

import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentEvent.Internal.Belief

/*
 * Ready-made event filters for `agent.wait(filter, timeout)`: each one returns what it matched, or `null` for any
 * other event, so that the waiting intention keeps waiting.
 */

/**
 * Matches the addition of a belief of type [B] satisfying [predicate], and returns that belief.
 */
inline fun <reified B : Any> beliefAdded(crossinline predicate: (B) -> Boolean = { true }): (AgentEvent) -> B? =
    { event -> ((event as? Belief.Add<*>)?.belief as? B)?.takeIf { predicate(it) } }

/**
 * Matches the removal of a belief of type [B] satisfying [predicate], and returns that belief.
 */
inline fun <reified B : Any> beliefRemoved(crossinline predicate: (B) -> Boolean = { true }): (AgentEvent) -> B? =
    { event -> ((event as? Belief.Remove<*>)?.belief as? B)?.takeIf { predicate(it) } }

/**
 * Matches a message whose payload is of type [P] and satisfies [predicate], and returns that message,
 * whose `sender` tells who sent it.
 */
@Suppress("UNCHECKED_CAST") // the payload is checked to be a P
inline fun <reified P : Any> messageReceived(
    crossinline predicate: (Message<P>) -> Boolean = { true },
): (AgentEvent) -> Message<P>? = { event ->
    (event as? Message<*>)?.takeIf { it.payload is P }?.let { it as Message<P> }?.takeIf { predicate(it) }
}

/**
 * Matches a perception of type [P] satisfying [predicate], and returns it.
 */
inline fun <reified P : Perception> perceived(crossinline predicate: (P) -> Boolean = { true }): (AgentEvent) -> P? =
    { event -> (event as? P)?.takeIf { predicate(it) } }
