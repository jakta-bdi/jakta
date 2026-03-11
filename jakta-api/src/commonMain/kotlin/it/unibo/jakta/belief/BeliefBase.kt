package it.unibo.jakta.belief

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
}
