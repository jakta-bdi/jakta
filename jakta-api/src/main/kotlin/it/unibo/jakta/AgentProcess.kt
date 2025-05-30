package it.unibo.jakta

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator

interface AgentProcess<Belief> : EventGenerator<Event.External> {
    fun percept(): Set<Belief>
}
