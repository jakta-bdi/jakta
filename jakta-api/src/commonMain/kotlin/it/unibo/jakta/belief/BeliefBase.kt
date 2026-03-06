package it.unibo.jakta.belief

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventInbox

// TODO support more complex Belief bases that have e.g. production rules for inference.
//  How would one customize this component?? Right now it is "hidden" inside the AgentImpl..

/**
 * Represents the belief base of an agent.
 * It is a mutable collection of beliefs, that can notify the agent of changes via events.
 */
interface BeliefBase<Belief : Any> : MutableCollection<Belief> {
    /**
     * Returns a snapshot of the current beliefs in the belief base.
     */
    fun snapshot(): Collection<Belief>

    /**
     * Factory methods for [BeliefBase].
     */
    companion object {
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
}
