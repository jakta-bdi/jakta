package it.unibo.jakta

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator

interface AgentProcess : EventGenerator<Event.External> {
    fun percept(): BeliefBase<*>
}
