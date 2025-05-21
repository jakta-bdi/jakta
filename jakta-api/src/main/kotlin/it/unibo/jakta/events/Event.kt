package it.unibo.jakta.events

import it.unibo.jakta.beliefs.Belief

interface Event {
    interface AgentEvent : Event
    interface EnvironmentEvent : Event
    interface BeliefEvent : Event
}
