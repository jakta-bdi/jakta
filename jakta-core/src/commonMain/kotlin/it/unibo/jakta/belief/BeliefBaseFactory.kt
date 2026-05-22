package it.unibo.jakta.belief

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventInbox

/**
 * Factory methods for [BeliefBase].
 */
object BeliefBaseFactory {
    /**
     * Creates an empty [BeliefBase].
     * @param[internalInbox] allows to send [AgentEvent.Internal.Belief] events to the agent.
     * @return the created empty [BeliefBase].
     */
    fun <Belief : Any> empty(internalInbox: EventInbox<AgentEvent.Internal.Belief<Belief>>): BeliefBase<Belief> =
        BeliefBaseImpl(internalInbox)

    /**
     * Creates a [BeliefBase] initialized with the given initial [beliefs].
     * @param[internalInbox] the channel to send internal belief events to the agent.
     * @param[beliefs] the initial beliefs to populate the belief base with.
     * @return the created BeliefBase.
     */
    fun <Belief : Any> of(
        internalInbox: EventInbox<AgentEvent.Internal.Belief<Belief>>,
        beliefs: Iterable<Belief>,
    ): BeliefBase<Belief> = BeliefBaseImpl(internalInbox, beliefs)
}
