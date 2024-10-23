package it.unibo.jakta.goals

/**
 * Global objective of the agent
 */
interface Goal<out State> {
    val desideratedState: State
}
