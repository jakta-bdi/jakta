package it.unibo.jakta.node

/**
 * Shared queue of events that are managed by [JaktaForAlchemistNode]s executing in the simulation.
 * Each [JaktaForAlchemistNode] holds its subscription to the [NodeNetwork].
 */
val NodeNetwork = SharedMemoryNetwork()
