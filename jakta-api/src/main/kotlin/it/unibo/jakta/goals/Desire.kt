package it.unibo.jakta.goals

/**
 * Global objective of the agent
 */
fun interface Desire<StateQuery> {
    /**
     * if false -> the current state does not satisfy the desire, do something.
     * if true -> the desire is reached.
     */
    fun testState(stateQuery: StateQuery): Boolean
}

//TODO("Come genero l'evento iniziale relativo a questa condizione iniziale?")
