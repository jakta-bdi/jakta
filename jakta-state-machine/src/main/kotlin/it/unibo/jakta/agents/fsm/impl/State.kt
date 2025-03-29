package it.unibo.jakta.agents.fsm.impl

/**
 * State values that an FSM can hold.
 */
enum class State {
    CREATED,
    STARTED,
    RUNNING,
    PAUSED,
    STOPPED,
}
